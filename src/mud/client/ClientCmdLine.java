// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.client;

import java.rmi.Naming;
import java.rmi.RemoteException;

import java.net.*;
import java.util.*;

import mud.common.*;
import mud.common.GameInfo.*;
import mud.interfaces.*;

class ClientCmdLine extends CmdLine implements Controller {

	private String client_version;
	
	private String naming_host;

	private static Game game;
	private static PlayerImpl player;
		
	private String username;
	private String password;
	
	private boolean logged_in = false;
	
	private String[][] usage = {
		{"u <user> <pass>", "set (u)ser"},
		{"l", "(l)ogin user"},
		{"o", "l(o)ok/(o)rient"},
		{"i", "(i)nventory"},
		{"c <item>", "(c)onsume food item"},
		{"e <room>", "(e)xit to room"},
		{"p <item>", "(p)ick up item"},
		{"a <player>", "(a)ttack player"},
		{"t", "(t)erminate combat"},
		{"echo", "send (e)cho signal"},
		{"ping", "send (p)ing signal"},
		{"s", "show (s)tack"},
		{"q", "(q)uit"},
		{"h", "(h)elp"}
	};
	

	public ClientCmdLine(String naming_host) {
		log = new ClientLogger();
		this.client_version = Config.version();
		this.naming_host = naming_host;
		puts("Client version "+version());
		log.debug("Accepting commands");
		acceptCommand();
	}

/**
 * Event handlers
 */

	
	public void broadcast(String msg) {
		bro(msg);
	}
	
	public void update(String msg) {
		upd(msg);
	}
	
	public void notify(String msg) {
		not(msg);
	}

	public void enteredRoom(String room_name) {
		not("Entered "+room_name);
		orient();
	}
	
	public void underAttack(String character) {
		ala("Under attack from "+character);
	}
	
	public void struck(String character, int damage, int health) {
		com("Struck "+character+" causing "+damage+" damage ["+health+"]");
	}
	
	public void wasStruck(String character, int damage, int health) {
		cor("Struck by "+character+" taking "+damage+" damage ["+health+"]");
	}
	
	public void killed(String character) {
		alo("Killed "+character);
	}
	
	public void wasKilled(String character) {
		ala("Killed by "+character);
	}

	public void attackStopped(String character) {
		alo(character+" terminated attack");
	}
	
/**
 * Command line methods
 */
	

	protected void orient() {
		try {
			List<String[]> is = game.examine_room(player, RoomInfo.NAME);
			put("Room: ", color_header);
			for (String[] i: is)
				putcs(i[0]);
			
			is = game.examine_room(player, RoomInfo.EXITS);
			putItems(is, new String[] {"Exits"}, "%s");
			
			is = game.examine_room(player, RoomInfo.HUMANS);
			putItems(is, new String[] {"Humans", "health", "weapon", "armor"},
				"%-10.10s  %8.8s  %8.8s  %7.7s");
			
			is = game.examine_room(player, RoomInfo.MONSTERS);
			putItems(is, new String[] {"Monsters", "health", "weapon", "armor"},
				"%-10.10s  %8.8s  %8.8s  %7.7s");

			is = game.examine_room(player, RoomInfo.WEAPONS);
			putItems(is, new String[] {"Weapons", "hitpoints"}, "%-15.15s  %12.12s");

			is = game.examine_room(player, RoomInfo.ARMOR);
			putItems(is, new String[] {"Armor", "strength"}, "%-17.17s  %10.10s");

			is = game.examine_room(player, RoomInfo.FOOD);
			putItems(is, new String[] {"Food", "value"}, "%-20.20s  %7.7s");
		
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}

	protected void inventory() {
		try {
			List<String[]> is = 
					game.check_inventory(player, CharacterInventory.WEAPONS);
			putItems(is, new String[] {"Weapons", "hitpoints"}, 
				"%-15.15s  %12.12s");

			is = game.check_inventory(player, CharacterInventory.ARMOR);
			putItems(is, new String[] {"Armor", "strength"}, 
				"%-17.17s  %10.10s");

			is = game.check_inventory(player, CharacterInventory.FOOD);
			putItems(is, new String[] {"Food", "value"}, "%-20.20s  %7.7s");
		
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}
	
	protected void exit(String input) {
		try {
			String match = match1args("e", input);
			if (match != null) {
				log.info("Exit to room "+match);
				game.exit_to(player, match);
			}
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}
	
	protected void pickup(String input) {
		try {
			String match = match1args("p", input);
			if (match != null) {
				log.info("Pick up item: "+match);
				game.pickup_item(player, match);
			}
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}
	
	protected void consume(String input) {
		try {
			String match = match1args("c", input);
			if (match != null) {
				log.info("Consume food: "+match);
				game.consume(player, match);
			}
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}
	
	protected void attack(String input) {
		try {
			String match = match1args("a", input);
			if (match != null) {
				log.info("Attack player: "+match);
				game.attack(player, match);
			}
		} catch (RemoteException e) { log.error(e.getMessage()); }
	}
	
	protected void terminate() {
		try {
			log.info("Terminate combat");
			game.terminate_combat(player);
		} catch (RemoteException e) { 
			log.error(e.getMessage()); 
//			e.printStackTrace();
		}
	}
	
	protected void loginUser() {
		try {
			log.info("Logging in user "+username);
			connect();
			checkVersion();
			game.log_in(username, password, player);
		} catch (RemoteException e) { log.error(e.getMessage()); }
		logged_in = true;
	}
	
	protected void setUser(String input) {
		String[] match = match2args("u", input);
		if (match != null) {
			username = match[0]; password = match[1];
			log.info("Setting user "+username);
			loginUser();
		} else {
			puts("Current user: "+username);
		}
	}
	
	protected void connect() {
		try {
			log.debug("Naming host on: "+naming_host);
			log.debug("IPs for host: "+
					InetAddress.getAllByName(naming_host).length);
			log.debug("IP for host: "+InetAddress.getByName(naming_host));
	
			log.info("Binding game object");
			game = (Game) Naming.lookup("//"+naming_host+"/Game");
			
			player = new PlayerImpl(this, log);
		} catch (Exception e) { 
//			log.warn(e.getMessage()); 
			e.printStackTrace();
		}
	}
	
	protected void checkVersion() {
		try {
			String server = game.query_version();
//			puts(server+" "+client_version);
			if (!client_version.equals(server)) {
				ala("Version mismatch: "+server+" [serv] / "+
					client_version+" [cli]");
			}
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	protected void sendPing() {
		try {
			log.info("ping");
			game.ping(player);
		} catch (RemoteException e) { log.warn(e.getMessage()); }
	}
	
	protected void sendEcho() {
		try {
			log.debug("Sending echo");
			puts(game.echo("Echo: client -> server -> client"));
		} catch (RemoteException e) {
			log.warn(e.getMessage());
		}
	}

	protected void displayPrompt() {
		pro(">> ");
	}
	
	protected void quit() {
		puts("");
		quit("");
	}

	protected void quit(String s) {
		try {
			if ((logged_in) && (game != null)) {
				game.send_disconnect(player);
			}
		} catch (RemoteException e) { log.error(e.getMessage()); }
		puts("Quitting game");
		System.exit(0);
	}



	protected void displayHelp() {
		super.displayHelp(usage);
	}
	
	protected void acceptCommand() {
		String input = "";
		while (true) {
			displayPrompt();
			input = readInput();
			if (input == null) quit();
			else if (input.matches("^u.*")) setUser(input);
			else if (input.matches("^l.*")) { if (userSet()) loginUser(); }
			else if (input.matches("^o\\s*")) { if (loggedIn()) orient(); }
			else if (input.matches("^i\\s*")) { if (loggedIn()) inventory(); }
			else if (input.matches("^echo")) sendEcho();
			else if (input.matches("^ping")) { if (userSet()) sendPing(); }
			else if (input.matches("^e.*")) { if (loggedIn()) exit(input); }
			else if (input.matches("^p.*")) { if (loggedIn()) pickup(input); }
			else if (input.matches("^c.*")) { if (loggedIn()) consume(input); }
			else if (input.matches("^a.*")) { if (loggedIn()) attack(input); }
			else if (input.matches("^t\\s*")) { if (loggedIn()) terminate(); }
			else if (input.matches("^s\\s*")) displayStack();
			else if (input.matches("^q\\s*")) quit(input);
			else if (input.matches("^h\\s*") || input.equals("")) displayHelp();
		}
	}
	
	
	private boolean userSet() {
		if (username == null) {
			err("Must set user first");
			return false;
		}
		return true;
	}
	
	private boolean loggedIn() {
		if (!logged_in) {
			err("Must log in first");
			return false;
		}
		return true;
	}
	
}
