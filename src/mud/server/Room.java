// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.util.*;

class Room extends Inventory {
	
	public String name;
	
	public List<Room> exits = new ArrayList<Room>();
	
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Monster> monsters = new ArrayList<Monster>();

	
	public Room() {}
	
	public Room(String name) {
		this.name = name;
	}
	

	public List<String[]> strName() {
		List<String[]> str = new ArrayList<String[]>();
		String[] s = { name };
		str.add(s);
		return str;
	}
	
	public List<String[]> strExits() {
		List<String[]> str = new ArrayList<String[]>();
		for (Room room: exits) {
			String[] s = { room.name };
			str.add(s);
		}
		return str;
	}

	public List<String[]> strUsers(boolean restrictedInfo) {
		List<String[]> str = new ArrayList<String[]>();
		for (User user: users) {
			String[] s;
			if (restrictedInfo)
				s = new String[] { user.username, user.last_host, name,
					user.human.getHealth()+"",
					user.human.getBestWeaponValue()+"",
					user.human.getStrongestArmorValue()+"" };
			else
				s = new String[] { user.username, user.human.getHealth()+"",
					user.human.getBestWeaponValue()+"",
					user.human.getStrongestArmorValue()+"" };
			str.add(s);
		}
		return str;
	}
	
	public List<String[]> strMonsters(boolean restrictedInfo) {
		List<String[]> str = new ArrayList<String[]>();
		for (Monster monster: monsters) {
			String[] s;
			if (restrictedInfo)
				s = new String[] { monster.name, name, 
					monster.aggressive+"", monster.getHealth()+"",
					monster.getBestWeaponValue()+"",
					monster.getStrongestArmorValue()+"" };
			else
				s = new String[] { monster.name, monster.getHealth()+"",
					monster.getBestWeaponValue()+"",
					monster.getStrongestArmorValue()+"" };
			str.add(s);
		}
		return str;
	}

}