// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

import mud.common.GameInfo.*;

public interface Game extends Remote {

/**
 * Connection accounting
 */
	
	void log_in(String username, String password, Player player) 
		throws RemoteException;
	void send_disconnect(Player player) throws RemoteException;


/**
 * User actions
 */

	List<String[]> examine_room(Player player, RoomInfo sw) 
		throws RemoteException;
	List<String[]> check_inventory(Player player, CharacterInventory sw) 
		throws RemoteException;
	void pickup_item(Player player, String item_name) throws RemoteException;
	void consume(Player player, String item_name) throws RemoteException;
	void attack(Player player, String char_name) throws RemoteException;
	void terminate_combat(Player player) throws RemoteException;
	void exit_to(Player player, String room_name) throws RemoteException;


/**
 * Debug methods
 */
	
	String query_version() throws RemoteException;
	String echo(String msg) throws RemoteException;
	void ping(Player player) throws RemoteException;
}
