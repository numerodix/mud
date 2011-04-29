// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.rmi.RemoteException;

import java.util.*;

import mud.common.*;

class Combat {
	
	private List<Character> attacked = new ArrayList<Character>();
	private List<Character> aggressors = new ArrayList<Character>();
	
	private CombatThread thread;
	
	public Combat(GameImpl game, Room room, Character agg, Character att) {
		this.attacked.add(att);
		this.aggressors.add(agg);
		att.combat = this;
		agg.combat = this;
		this.thread =
			new CombatThread(game, room, aggressors, attacked);
		thread.start();
	}
	
	public void join(Character agg, Character att) {
		if (agg == att) return;
		
		if (aggressors.contains(agg)) {
			if (!attacked.contains(att)) attacked.add(att);
		} else if (attacked.contains(agg)) {
			if (!aggressors.contains(att)) aggressors.add(att);
		} else if (aggressors.contains(att)) {
			if (!attacked.contains(agg)) attacked.add(agg);
		} else if (attacked.contains(att)) {
			if (!aggressors.contains(agg)) aggressors.add(agg);
		}
		
		agg.combat = this;
		att.combat = this;
	}
	
	public void leave(Character ch) throws RemoteException {
		if (attacked.contains(ch)) {
			throw new RemoteException("Only aggressor can terminate combat");
		} else if (aggressors.contains(ch)) {
			aggressors.remove(ch);
			if (aggressors.size() == 0) {
				for (Character c: attacked) c.combat = null;
				thread.terminate();
			}
		}
	}
	
	public void escape(Character ch) {
		if (attacked.contains(ch)) {
			attacked.remove(ch);
			ch.combat = null;
			if (attacked.size() == 0) {
				for (Character c: aggressors) c.combat = null;
				thread.terminate();
			}
		} else if (aggressors.contains(ch)) {
			aggressors.remove(ch);
			ch.combat = null;
			if (aggressors.size() == 0) {
				for (Character c: attacked) c.combat = null;
				thread.terminate();
			}
		}
	}
	
	public List<User> getOpposition(Character ch) {
		List<User> is = new ArrayList<User>();
		if (attacked.contains(ch)) {
			for (Character c: aggressors) {
				if (c instanceof Human) {
					is.add( ((Human) c).user );
				}
			}
		}
		if (aggressors.contains(ch)) {
			for (Character c: attacked) {
				if (c instanceof Human) {
					is.add( ((Human) c).user );
				}
			}
		}
		return is;
	}
	
	public List<String> getOppositionStr(Character ch) {
		List<String> is = new ArrayList<String>();
		if (attacked.contains(ch)) {
			for (Character c: aggressors) is.add(c.name);
		}
		if (aggressors.contains(ch)) {
			for (Character c: attacked) is.add(c.name);
		}
		return is;
	}

}

abstract class Damage {
	
	public Room room;
	public Character source;
	public User source_user;
	public Character target;
	public User target_user;
	
}

class Blow extends Damage {
	
	public int damage;
	public int new_health;
	
	public Blow(Room room, Character source, Character target,
	int damage, int new_health) {
		if (source instanceof Human) source_user = ((Human) source).user;
		if (target instanceof Human) target_user = ((Human) target).user;
		this.room = room;
		this.source = source;
		this.target = target;
		this.damage = damage;
		this.new_health = new_health;
	}
	
}

class Kill extends Damage {
	
	public Weapon weapon;
	public Armor armor;
	
	
	public Kill(Room room, Character source, Character target,
	Weapon weapon, Armor armor) {
		if (source instanceof Human) source_user = ((Human) source).user;
		if (target instanceof Human) target_user = ((Human) target).user;
		this.room = room;
		this.source = source;
		this.target = target;
		this.weapon = weapon;
		this.armor = armor;
	}
	
}

class CombatThread extends Thread {
	
	private boolean game_on = true;
	
	public Logger log;
	private GameImpl game;
	private Room room;
	Random random = new Random();
	
	private long combat_frame = 3000L;
	
	private List<Character> aggressors;
	private List<Character> attacked;
	
	private List<Kill> kills = new ArrayList<Kill>();
	private List<Blow> blows = new ArrayList<Blow>();
	
	
	public CombatThread(GameImpl game, Room room,
	List<Character> aggressors, List<Character> attacked) {
		this.game = game;
		this.room = room;
		this.aggressors = aggressors;
		this.attacked = attacked;
		this.setName(aggressors.get(0).name + " vs "+attacked.get(0).name);
		log = new ServerLogger();
	}
	
	private synchronized void initFrame() {
		kills.clear();
		blows.clear();
	}
	
	private synchronized void playFrame() {
		if ((aggressors.size() > 0) && (attacked.size() > 0)) {
			for (Character c: aggressors) {
				Character t = getPuniest(attacked);
				strike(c, t);
			}
			for (Character c: attacked) {
				Character t = getPuniest(aggressors);
				strike(c, t);
			}
		}
	}
	
	private void strike(Character src, Character tar) {
		int hitpoints = src.getBestWeaponValue();
		int shield = tar.getStrongestArmorValue();
		
		int strike_potential = hitpoints - shield;
		int damage = 1;
		if (strike_potential > 1) {
			damage = ((strike_potential+1) / 2)
				+ random.nextInt((strike_potential / 2)+1);
		}
		
		tar.decHealth(damage);
		if (tar.health <= 0) {
			if (findKill(tar) == null) {
				kills.add(new Kill(room, src, tar, tar.getBestWeapon(),
					tar.getStrongestArmor()));
			}
		} else {
			blows.add(new Blow(room, src, tar, damage, tar.health));
		}
	}
	
	private Kill findKill(Character target) {
		for (Kill k: kills) if (k.target == target) return k;
		return null;
	}
	
	private Character getPuniest(List<Character> is) {
		int ferocity = 201; Character puny = null;
		for (Character t: is) {
			if (ferocity > t.getFerocity()) {
				ferocity = t.getFerocity();
				puny = t;
			}
		}
		return puny;
	}
	
	private synchronized void postProcessFrame() {
		for (Kill k: kills) {
			k.target.combat = null;
			
			Weapon w = k.weapon;
			Armor a = k.armor;
			
			// give victim's best goods to slayer
			if (w != null) {
				room.weapons.add(w);
				k.source.pickup(w, room);
			}
			if (a != null) {
				room.armor.add(a);
				k.source.pickup(a, room);
			}
			
			// remove from room
			if (k.target_user != null) {
				room.users.remove(k.target_user);
				k.target_user.human = null;
			}
			else room.monsters.remove(k.target);
			
			if (aggressors.contains(k.target)) aggressors.remove(k.target);
			if (attacked.contains(k.target)) attacked.remove(k.target);
		}
		if ((aggressors.size() == 0) || (attacked.size() == 0)) {
			for (Character c: aggressors) c.combat = null;
			for (Character c: attacked) c.combat = null;
			terminate();
		}
	}
	
	public void run() {
		try {
			int frame = 0;
			
			while (game_on) {
				Thread.sleep(combat_frame/2);
				
				initFrame();
				playFrame();
				postProcessFrame();
				
				game.report_blows(blows);
				game.report_kills(kills);
				
				Thread.sleep(combat_frame);
				frame++;
			}
		} catch (InterruptedException e) {
			log.warn("Combat thread interrupted");
		}
	}
	
	public void terminate() {
		game_on = false;
	}
	
}