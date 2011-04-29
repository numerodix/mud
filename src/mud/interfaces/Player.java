// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

import mud.common.GameInfo.*;

public interface Player extends Remote {

/**
 * Events triggered by my actions
 */

	void entered_room(String room_name) throws RemoteException;
	void picked_up(String item, String olditem) throws RemoteException;
	void healed(String food, int value, int newhealth) throws RemoteException;
	void attacked(String character) throws RemoteException;
	void struck(String character, int damage, int health) throws RemoteException;
	void killed(String character) throws RemoteException;
	void terminated() throws RemoteException;

/**
 * Events that affect me
 */
	
	void under_attack(String character) throws RemoteException;
	void was_struck(String character, int damage, int health)
		throws RemoteException;
	void was_killed(String character) throws RemoteException;
	void attack_stopped(String character) throws RemoteException;
	void send_disconnect(String username, String host) throws RemoteException;
	
/**
 * Updates about other players in room
 */

	void enters_room(CharacterType ct, String name) throws RemoteException;
	void exits_room(CharacterType ct, String name) throws RemoteException;
	void picks_up(String name, String item, String olditem)
		throws RemoteException;
	void attacks(String aggressor, String target) throws RemoteException;
	void strikes(String aggressor, String target, int damage, int health)
		throws RemoteException;
	void kills(String aggressor, String target) throws RemoteException;
	void terminates(String aggressor, List<String> targets)
		throws RemoteException;

/**
 * Broadcasts
 */

	void broadcast_new_rooms(List<String> rooms) throws RemoteException;
	void broadcast_monster_spawns(List<String> monsters) throws RemoteException;
	void broadcast_user_respawns(List<String> users, String room)
		throws RemoteException;
	void broadcast_kill(String aggressor, String target) throws RemoteException;
	void broadcast_connect(String username) throws RemoteException;
	void broadcast_disconnect(String username) throws RemoteException;

/**
 * Debug methods
 */
	String echo(String msg) throws RemoteException;
	void pong() throws RemoteException;

}
