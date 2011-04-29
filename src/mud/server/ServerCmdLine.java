// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

import java.util.*;

import mud.common.*;
import mud.common.GameInfo.*;

class ServerCmdLine extends CmdLine {

	private GameImpl game;
	
	protected String[][] usage = { 
			{"users", "display users"},
			{"godmode", "heal and arm all users"},
			{"monsters", "display monsters"},
			{"rooms", "display rooms"},
			{"s", "show (s)tack"},
			{"halt", "(h)alt"},
			{"h", "(h)elp"}
		};

	
	public ServerCmdLine() {
		log = new ServerLogger();
		puts("Server version "+version());
		initRMI();
		acceptCommand();
	}

	private void initRMI() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		try {
			if (game == null) game = new GameImpl(log, new RoomFactory());
			Naming.rebind("Game", game);
			log.debug("Game bound in registry");
		} catch (Exception e) { 
//			log.warn(e.getMessage()); 
			e.printStackTrace();
		}
	}

	protected void displayChars(CharacterType ct) {
		List<String[]> is = new ArrayList<String[]>();
		String labels[] = null;
		String fmt = "";
		if (ct == CharacterType.HUMAN) {
			if (game.users.size() == 0) { puts("No users in the game"); return; }
			
			for (Room room: game.rooms) {
				is.addAll(room.strUsers(true));
			}
			labels = new String[] 
				{"username", "last hostname", "room", "health", "weapon", "armor"};
			fmt = "%-10.10s  %-17.17s  %-16.16s  %8.8s  %8.8s  %7.7s";
		} else if (ct == CharacterType.MONSTER) {
			for (Room room: game.rooms) {
				is.addAll(room.strMonsters(true));
			}
			labels = new String[] 
				{"name", "room", "agg", "health", "weapon", "armor"};
			fmt = "%-16.16s  %-16.16s  %-5.5s  %8.8s  %8.8s  %7.7s";
		}
		putItems(is, labels, fmt);
	}
	
	protected synchronized void godMode() {
		for (User u: game.users) {
			if (u.human != null) {
				u.human.incHealth(100);
				u.human.weapons.add(game.roomfac.getBestWeapon());
				u.human.armor.add(game.roomfac.getStrongestArmor());
			}
		}
	}
	
	protected void displayRooms() {
		List<String[]> is = new ArrayList<String[]>();
		String labels[] = new String[] {"name", "exits"};
		String fmt = "%-35s  %s";
		for (Room r: game.rooms) {
			is.add(new String[] {r.name, r.exits.size()+""});
		}
		putItems(is, labels, fmt);
	}

	protected void displayPrompt() {
		pro("serv> ");
	}
	
	protected void halt() {
		log.info("Halting server");
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
			if (input == null) input = "";
			if (input.matches("^users")) displayChars(CharacterType.HUMAN);
			if (input.matches("^godmode")) godMode();
			else if (input.matches("^monsters")) displayChars(CharacterType.MONSTER);
			else if (input.matches("^rooms")) displayRooms();
			else if (input.matches("^s\\s*")) displayStack();
			else if (input.matches("^halt")) halt();
			else if (input.matches("^h\\s*") || input.equals("")) displayHelp();
		}
	}
}
