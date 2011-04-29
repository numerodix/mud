// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;

import mud.common.*;
import mud.common.GameInfo.*;
import mud.interfaces.*;

public class PlayerImpl extends UnicastRemoteObject implements Player {

	private Logger log;
	private Controller contr;

	public PlayerImpl(Controller contr, Logger log) throws RemoteException {
		super();
		this.contr = contr;
		this.log = log;
	}


/**
 * Events triggered by my actions
 */
	
	public void entered_room(String room_name) throws RemoteException {
		contr.enteredRoom(room_name);
	}
	
	public void picked_up(String item, String olditem) throws RemoteException {
		String s = "";
		if (olditem != null) s = ", dropped "+olditem;
		contr.notify("Picked up "+item+s);
	}
	
	public void healed(String food, int value, int newhealth) 
	throws RemoteException {
		contr.notify("Gained "+value+" from "+food+", health now "+ newhealth);
	}
	
	public void attacked(String character) throws RemoteException {
		contr.notify("Attacked "+character);
	}
	
	public void struck(String character, int damage, int health)
	throws RemoteException {
		contr.struck(character, damage, health);
	}
	
	public void killed(String character) throws RemoteException {
		contr.killed(character);
	}
	
	public void terminated() throws RemoteException {
		contr.notify("Terminated combat");
	}


/**
 * Events that affect me
 */
	
	public void under_attack(String character) throws RemoteException {
		contr.underAttack(character);
	}
	
	public void was_struck(String character, int damage, int health)
	throws RemoteException {
		contr.wasStruck(character, damage, health);
	}
	
	public void was_killed(String character) throws RemoteException {
		contr.wasKilled(character);
	}
	
	public void attack_stopped(String character) throws RemoteException {
		contr.attackStopped(character);
	}

	public void send_disconnect(String username, String host) 
	throws RemoteException {
		contr.notify("Server disconnected "+username
			+", now connected from "+host);
	}


/**
 * Updates about other players in room
 */
	
	public void enters_room(CharacterType ct, String name) throws RemoteException {
		switch (ct) {
			case HUMAN: contr.update("User "+name+" entered the room"); break;
			case MONSTER: contr.update("Monster "+name+" entered the room"); break;
		}
	}
	
	public void exits_room(CharacterType ct, String name) throws RemoteException {
		switch (ct) {
			case HUMAN: contr.update("User "+name+" left the room"); break;
			case MONSTER: contr.update("Monster "+name+" left the room"); break;
		}
	}
	
	public void picks_up(String name, String item, String olditem)
	throws RemoteException {
		String s = "";
		if (olditem != null) s = ", dropped "+olditem;
		contr.update(name+" picked up "+item+s);
	}
	
	public void attacks(String aggressor, String target)
	throws RemoteException {
		contr.update(aggressor+" attacks "+target);
	}
	
	public void strikes(String aggressor, String target, int damage, int health)
	throws RemoteException {
		contr.update(aggressor+" strikes "+target
				+" inflicting "+damage+" damage ["+health+"]");
	}
	
	public void kills(String aggressor, String target)
	throws RemoteException {
		contr.update(aggressor+" kills "+target);
	}
	
	public void  terminates(String aggressor, List<String> targets)
	throws RemoteException {
		String t = "";
		for (String s: targets) t += " "+s;
		contr.update(aggressor+" terminates attack on"+t);
	}

/**
 * Broadcasts
 */
	
	public void broadcast_new_rooms(List<String> rooms) throws RemoteException {
		for (String r: rooms) contr.broadcast("New room "+r+" opened");
	}
	
	public void broadcast_monster_spawns(List<String> monsters)
	throws RemoteException {
		for (String m: monsters) contr.broadcast("New monster "+m+" spawned");
	}
	
	public void broadcast_user_respawns(List<String> users, String room)
	throws RemoteException {
		for (String u: users) contr.broadcast("User "+u+" respawned in "+room);
	}
	
	public void broadcast_kill(String aggressor, String target)
	throws RemoteException {
		contr.broadcast(aggressor+" killed "+target);
	}
	
	public void broadcast_connect(String username) throws RemoteException {
		contr.broadcast("User "+username+" connected");
	}
	
	public void broadcast_disconnect(String username) throws RemoteException {
		contr.broadcast("User "+username+" disconnected");
	}

/**
 * Debug methods
 */

	public String echo(String msg) throws RemoteException {
		log.info("From Server: "+msg);
		return msg;
	}
	
	public void pong() throws RemoteException {
		log.info("pong");
	}
	
}
