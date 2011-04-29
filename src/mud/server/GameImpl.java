// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;

import mud.common.*;
import mud.common.GameInfo.*;
import mud.interfaces.*;

public class GameImpl extends UnicastRemoteObject implements Game {

	public String server_version;
	
	public Logger log;
	public RoomFactory roomfac;
	public GameTicker ticker;
	
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Room> rooms = new ArrayList<Room>();
	
	private double user_room_factor = 3.5;
	private double monster_room_factor = 1.5;


	public GameImpl(Logger log, RoomFactory roomfac) throws RemoteException {
		super();
		this.log = log;
		this.server_version = Config.version();
		ticker = new GameTicker(this, log);
		this.roomfac = roomfac;
		roomfac.rooms = rooms;
		roomfac.createRooms(5);
		roomfac.createMonsters(5);
	}


/**
 * Remote methods: Connection accounting
 */

	public void log_in(String username, String password, Player player) 
	throws RemoteException {
		User user;
		Player pplayer;
		String phost;
		
		synchronized (this) {
			user = findUser(username);
			if (user == null)
				user = createUser(username, password, player);
			else if (!password.equals(user.password))
				throw new RemoteException("Invalid credentials for "+username);
			
			pplayer = user.player;
			phost = user.last_host;
			
			user.player = player;
			user.last_host = getRemoteHost();
			
			wakeUpCharacter(user);
		}

		notify_user_disconnect(pplayer, username, phost, user.last_host);
		
		try {
			user.player.entered_room(findRoom(user).name);
			broadcast_connect(user.username);
			notify_room_entry(user, rooms.get(0));
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	public void send_disconnect(Player player) throws RemoteException {
		User user = findUser(player);
		if (user != null) {
			log.info("User "+user.username+" disconnects");
			broadcast_disconnect(user.username);
			user.player = null;
		}
	}
	

/**
 * Remote methods: User actions
 */

	public List<String[]> examine_room(Player player, RoomInfo sw) 
	throws RemoteException {
		demandAlive(player);
		
		synchronized (this) {
			Room room = findRoom(findUser(player));
			switch (sw) {
				case NAME: return room.strName();
				case EXITS: return room.strExits();
				case HUMANS: return room.strUsers(false);
				case MONSTERS: return room.strMonsters(false);
				case WEAPONS: return room.strWeapons();
				case ARMOR: return room.strArmor();
				case FOOD: return room.strFood();
			}
		}
		
		return null;
	}
	
	public List<String[]> check_inventory(Player player, CharacterInventory sw) 
	throws RemoteException {
		demandAlive(player);
		
		synchronized (this) {
			User user = findUser(player);
			switch (sw) {
				case WEAPONS: return user.human.strWeapons();
				case ARMOR: return user.human.strArmor();
				case FOOD: return user.human.strFood();
			}
		}
		
		return null;
	}
		
	public void pickup_item(Player player, String item_name) 
	throws RemoteException {
		demandAlive(player);
		demandNotInCombat(player);
		User user = findUser(player);
		pickupItem(user, item_name);
	}
	
	public void consume(Player player, String item_name) 
	throws RemoteException {
		demandAlive(player);
		User user = findUser(player);
		consumeFood(user, item_name);
	}
	
	public void attack(Player player, String char_name) throws RemoteException {
		demandAlive(player);
		demandNotInCombat(player);
		User user = findUser(player);
		attackPlayer(user, char_name);
	}
	
	public void terminate_combat(Player player) throws RemoteException {
		demandAlive(player);
		User user = findUser(player);
		terminateCombat(user);
	}
	
	public void exit_to(Player player, String room_name) throws RemoteException {
		demandAlive(player);
		User user = findUser(player);
		moveCharacter(user, room_name);
	}
	

/**
 * Remote methods: Debug methods
 */
	
	public String query_version() throws RemoteException {
		return server_version;
	}
	
	public String echo(String msg) throws RemoteException {
		return msg;
	}

	public void ping(Player player) throws RemoteException {
		log.debug("Received ping");
		if (player == null) {
			log.debug("Cannot send pong, playback object is null");
			throw new RemoteException("Player callback object is null");
		}
		log.debug("Sending pong");
		player.pong();
	}


/**
 * Gameplay methods
 */

	private void wakeUpCharacter(User user) {
		if (user.human == null) {
			user.human = roomfac.getHuman(user);
			rooms.get(0).users.add(user);
		}
	}

	private void moveCharacter(User user, String room_regex) 
	throws RemoteException {
		log.debug("User "+user.username+" requested exit to "+room_regex);
		
		Room r; Room rn; List<String> monsters;
		synchronized (this) {
			r = findRoom(user);
			rn = findRoomExit(r, room_regex);
			if (rn == null) throw new RemoteException("No room "+room_regex);
			
			if (user.human.combat != null) {
				user.human.combat.escape(user.human);
			}
			
			r.users.remove(user);
			rn.users.add(user);
			
			monsters = triggerAggressiveMonsters(rn, user);
		}
			
		notify_room_exit(user, r);
		notify_room_entry(user, rn);
		
		try {
			user.player.entered_room(rn.name);
			for (String att: monsters) user.player.under_attack(att);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void pickupItem(User user, String item_regex)
	throws RemoteException {
		Room room;
		Item it; Item itold; String itold_name = null;
		
		synchronized (this) {
			room = findRoom(user);
			it = findItem(room, item_regex);
			if (it == null) 
				throw new RemoteException("No item "+item_regex);
			itold = user.human.pickup(it, room);
			if (itold != null) itold_name = itold.name;
		}
		
		try {
			user.player.picked_up(it.name, itold_name);
			notify_room_pickup(user, it.name, itold_name);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void consumeFood(User user, String item_regex)
	throws RemoteException {
		Food f;
		
		synchronized (this) {
			f = user.human.findFood(item_regex);
			if (f == null) 
				throw new RemoteException("Have no food "+item_regex);
			if (!user.human.consume(f))
				throw new RemoteException("Not hungry");
		}
		
		try {
			user.player.healed(f.name, f.getValue(), user.human.health);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void attackPlayer(User user, String char_regex)
	throws RemoteException {
		Room room; Character c; List<User> opposition; String target;
		
		synchronized (this) {
			room = findRoom(user);
			c = findCharacter(room, char_regex);
			if ((c == null) || (c == user.human))
				throw new RemoteException("No valid character "+char_regex);
			if (c.combat != null)
				c.combat.join(user.human, c);
			else
				user.human.combat = new Combat(this, room, user.human, c);
			opposition = user.human.combat.getOpposition(user.human);
			target = c.name;
		}
		
		try {
			user.player.attacked(c.name);
			notify_user_attack(opposition, user);
			notify_room_startcombat(user, target, room);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void terminateCombat(User user) throws RemoteException {
		Room room; List<User> opposition; List<String> opposition_str;
		
		synchronized (this) {
			room = findRoom(user);
			if (user.human.combat == null)
				throw new RemoteException("Not engaged in combat");
			opposition = user.human.combat.getOpposition(user.human);
			opposition_str = user.human.combat.getOppositionStr(user.human);
			user.human.combat.leave(user.human);
			user.human.combat = null;
		}
		
		try {
			user.player.terminated();
			notify_user_termination(opposition, user);
			notify_room_endcombat(user, opposition_str, room);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
		
	}

	
/**
 * Local methods
 */

	private synchronized User findUser(String username) {
		for (User user: users) {
			if (user.username.equals(username)) return user;
		}
		return null;
	}

	private synchronized User findUser(Player player) {
		for (User user: users) {
			if (player.equals(user.player)) return user;
		}
		return null;
	}
	
	private synchronized User createUser(String username, String password, 
	Player player) {
		User user = new User(username, password, player, getRemoteHost());
		users.add(user);
		return user;
	}
	
	public synchronized Room findRoom(User user) {
		for (Room r: rooms) {
			if (r.users.contains(user)) return r;
		}
		return null;
	}

	private synchronized Room findRoomExit(Room room, String room_regex) {
		for (Room r: room.exits) {
			if (matches(r.name, room_regex)) return r;
		}
		return null;
	}
	
	private synchronized Item findItem(Room room, String item_regex) {
		ArrayList<Item> is = new ArrayList<Item>();
		is.addAll(room.weapons);
		is.addAll(room.armor);
		is.addAll(room.food);
		for (Item i: is) {
			if (matches(i.name, item_regex)) return i;
		}
		return null;
	}
	
	private synchronized Character findCharacter(Room room, String char_regex) {
		ArrayList<Character> is = new ArrayList<Character>();
		for (User u: room.users) is.add(u.human);
		is.addAll(room.monsters);
		for (Character i: is) {
			if (matches(i.name, char_regex)) return i;
		}
		return null;
	}
	

/**
 * Notifier methods
 * Should not alter game state as we cannot synchronize them!
 */

	public void report_blows(List<Blow> blows) {
		try {
			for (Blow b: blows) {
				if ((b.source_user != null) && (b.source_user.player != null)) {
					b.source_user.player.struck(b.target.name, b.damage,
						b.new_health);
				}
				if ((b.target_user != null) && (b.target_user.player != null)) {
					b.target_user.player.was_struck(b.source.name, b.damage,
						b.new_health);
				}
				notify_room_strike(b.room, b.source.name, b.target.name,
					b.damage, b.new_health);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}

	public void report_kills(List<Kill> kills) {
		try {
			for (Kill k: kills) {
				if ((k.source_user != null) && (k.source_user.player != null)) {
					k.source_user.player.killed(k.target.name);
				}
				if ((k.target_user != null) && (k.target_user.player != null)) {
					k.target_user.player.was_killed(k.source.name);
				}
				notify_room_kill(k.room, k.source.name, k.target.name);
				broadcast_kill(k.source.name, k.target.name);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}

/**
 * Notifier methods : Events that affect player
 */
	
	private void notify_user_attack(List<User> opposition, User user) {
		try {
			for (User u: opposition) {
				if ((u.player != null) && (u != user))
					u.player.under_attack(user.username);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	public void notify_user_termination(List<User> opposition, User user) {
		try {
			for (User u: opposition) {
				if ((u.player != null) && (u != user))
					u.player.attack_stopped(user.username);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
		
	private void notify_user_disconnect(Player pplayer, String username, 
	String phost, String host) {
		/* if user logs in from different location the previous location
		receives a notification to this effect if the ips differ */
		try {
			if ((pplayer != null) && (!host.equals(phost)))
					pplayer.send_disconnect(username, host);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
/**
 * Notifier methods : Updates about other players in room
 */
	
	private void notify_room_entry(User user, Room room) {
		log.debug("Notify entry of "+user.username+" to users in "+room.name);
		try {
			for (User u: room.users) {
				if ((u.player != null) && (user != u))
					u.player.enters_room(CharacterType.HUMAN, user.username);
			}
		} catch (RemoteException e) { 
			log.warn(e.getMessage());
//			e.printStackTrace();
		}
	}
	
	private void notify_room_exit(User user, Room room) {
		log.debug("Notify exit of "+user.username+" to users in "+room.name);
		try {
			for (User u: room.users) {
				if ((u.player != null) && (user != u))
					u.player.exits_room(CharacterType.HUMAN, user.username);
			}
		} catch (RemoteException e) { 
			log.warn(e.getMessage());
//			e.printStackTrace();
		}
	}
	
	private void notify_room_pickup(User user, String item, String olditem) {
		try {
			Room room = findRoom(user);
			for (User u: room.users) {
				if ((u.player != null) && (u != user))
					u.player.picks_up(user.username, item, olditem);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}

	private void notify_room_startcombat(User user, String target, Room room) {
		try {
			for (User u: room.users) {
				if ((u.player != null) 
					&& (u != user) && (!u.username.equals(target)))
					u.player.attacks(user.username, target);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void notify_room_strike(Room room, String source, String target,
	int damage, int new_health) {
		try {
			for (User u: room.users) {
				if ((u.player != null)
					&& (!u.username.equals(source))
					&& (!u.username.equals(target)))
					u.player.strikes(source, target, damage, new_health);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void notify_room_kill(Room room, String source, String target) {
		try {
			for (User u: room.users) {
				if ((u.player != null)
					&& (!u.username.equals(source))
					&& (!u.username.equals(target)))
					u.player.kills(source, target);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void notify_room_endcombat(User user, List<String> targets, Room room) {
		try {
			for (User u: room.users) {
				if ((u.player != null) 
					&& (u != user) && (!targets.contains(u.username)))
					u.player.terminates(user.username, targets);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}

/**
 * Notifier methods : Broadcasts
 */

	private void broadcast_new_rooms(List<String> rooms) {
		try {
			for (User user: users) {
				if (user.player != null)
					user.player.broadcast_new_rooms(rooms);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void broadcast_monster_spawns(List<String> monsters) {
		try {
			for (User user: users) {
				if (user.player != null)
					user.player.broadcast_monster_spawns(monsters);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void broadcast_user_respawns(List<String> usrs, String room) {
		try {
			for (User user: users) {
				if (user.player != null)
					user.player.broadcast_user_respawns(usrs, room);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void broadcast_kill(String source, String target) {
		try {
			for (User user: users) {
				if ((user.player != null)
					&& (!user.username.equals(source))
					&& (!user.username.equals(target)))
					user.player.broadcast_kill(source, target);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void broadcast_connect(String username) {
		try {
			for (User user: users) {
				if ((user.player != null) && (!username.equals(user.username)))
					user.player.broadcast_connect(username);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	private void broadcast_disconnect(String username) {
		try {
			for (User user: users) {
				if ((user.player != null) && (!username.equals(user.username)))
					user.player.broadcast_disconnect(username);
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}


/**
 * Server side services
 */

	public void invokeRespawnUsers() {
		List<String> users_respawned = new ArrayList<String>();
		
		synchronized (this) {
			for (User u: users) {
				if (u.human == null) {
					u.human = roomfac.getHuman(u);
					rooms.get(0).users.add(u);
					users_respawned.add(u.username);
				}
			}
		}
		
		if (users_respawned.size() != 0)
			broadcast_user_respawns(users_respawned, rooms.get(0).name);
	}
		
	public void invokeRoomAdjustment() {
//		log.debug("Room adjustment started");
		List<String> new_rooms = adjustRoomNumber();
		if (new_rooms != null)
			broadcast_new_rooms(new_rooms);
	}

	public void invokeMonsterAdjustment() {
//		log.debug("Room adjustment started");
		List<String> new_monsters = adjustMonsterNumber();
		if (new_monsters != null)
			broadcast_monster_spawns(new_monsters);
	}
	
	public void invokeHealHumans() {
//		log.debug("Room adjustment started");
		healHumans();
	}


/**
 * Helper methods
 */

	
	private void demandLoggedIn(Player player) throws RemoteException {
		User user = findUser(player);
		if (user == null)
			throw new RemoteException("Not logged in");
	}
	
	private void demandAlive(Player player) throws RemoteException {
		demandLoggedIn(player);
		User user = findUser(player);
		if (user.human == null)
			throw new RemoteException(
				"Your character is not alive, wait to be respawned or re-login");
	}
	
	private void demandNotInCombat(Player player) throws RemoteException {
		User user = findUser(player);
		if (user.human.combat != null)
			throw new RemoteException("Not now, engaged in combat");
	}
	
	
	private String getRemoteHost() {
		try {
			return UnicastRemoteObject.getClientHost();
 		} catch (Exception e) { log.warn(e.getMessage()); }
		return null;
	}
	
	private boolean matches(String s1, String regex) {
		return s1.toLowerCase().matches(".*"+regex.toLowerCase()+".*");
	}
	
	private synchronized List<String> adjustRoomNumber() {
		List<String> nrooms = new ArrayList<String>();
		int number_of_rooms = (int) (users.size() * user_room_factor);
		int delta = number_of_rooms - rooms.size();
//		log.debug("Number of rooms: "+number_of_rooms+", delta: "+delta);
		if (delta > 0) {
			nrooms = roomfac.createRooms(1);
		}
		return nrooms;
	}
	
	private synchronized List<String> adjustMonsterNumber() {
		List<String> nmonsters = new ArrayList<String>();
		int monster_count = 0;
		for (Room r: rooms) { monster_count += r.monsters.size(); }
		
		int number_of_monsters = (int) (rooms.size() * monster_room_factor);
		int delta = number_of_monsters - monster_count;
		
//		log.debug("Number of monsters: "+number_of_monsters+", delta: "+delta);
		if (delta > 0) {
			nmonsters = roomfac.createMonsters(1);
		}
		return nmonsters;
	}
	
	private synchronized void healHumans() {
		for (User u: users) {
			if (u.human != null) u.human.incHealth(3);
		}
	}
	
	private synchronized List<String> triggerAggressiveMonsters(Room room,
	User user) {
		List<String> monsters = new ArrayList<String>();
		for (Monster m: room.monsters) {
			if (m.aggressive) {
				if (m.combat != null) {
					m.combat.join(m, user.human);
				} else if (user.human.combat != null) {
					user.human.combat.join(m, user.human);
				}
				else {
					m.combat = new Combat(this, room, m, user.human);
				}
				monsters.add(m.name);
			}
		}
		return monsters;
	}
	
	
}
