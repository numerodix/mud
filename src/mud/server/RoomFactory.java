// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.io.*;
import java.util.*;

class RoomFactory {
	
	BufferedReader br;
	Random random = new Random();
	
	List<String> room_names = new ArrayList<String>();
	List<String> room_types = new ArrayList<String>();
	List<String> monster_names = new ArrayList<String>();
	List<String[]> weapons = new ArrayList<String[]>();
	List<String[]> armor = new ArrayList<String[]>();
	List<String[]> food = new ArrayList<String[]>();

	String path = "../data/";
	
	public ArrayList<Room> rooms;
	
	final private int max_exits = 4;
	final private int room_weapons = 3;
	final private int room_armor = 3;
	final private int room_food = 3;
	
	final private int monster_weapons = 2;
	final private int monster_armor = 1;
	final private int monster_health = 50;
	
	
	public RoomFactory() throws Exception {
		room_names = readLines(path+"room-names.data");
		room_types = readLines(path+"room-types.data");
		monster_names = readLines(path+"monster-names.data");
		weapons = splitLines(readLines(path+"weapons.data"));
		armor = splitLines(readLines(path+"armor.data"));
		food = splitLines(readLines(path+"food.data"));
	}
	

	public Human getHuman(User user) {
		Human h = new Human(user);
		h.weapons.add(getPuniestWeapon());
		return h;
	}
	
	public List<String> createRooms(int amount) {
		if (rooms.size() == 0) {
			rooms.add(new Room("The Lobby"));
		}
		
		amount = random.nextInt(amount)+1;
		List<String> new_rooms = new ArrayList<String>();
		for (; amount > 0; amount--) {
			int exits = random.nextInt(max_exits)+1;
			Room r = createRoom();
			for (; exits > 0; exits--) {
				Room ref = rooms.get(getFullRoomNum());
				if ((!r.exits.contains(ref)) && (r.exits.size() < max_exits))
					r.exits.add(ref);
				if ((!ref.exits.contains(r)) && (ref.exits.size() < max_exits))
					ref.exits.add(r);
			}
			new_rooms.add(r.name);
			rooms.add(r);
		}
		return new_rooms;
	}
	
	public Room createRoom() {
		Room r = new Room();
		r.name = getRoomName();
		setItems(r);
		return r;
	}

	public List<String> createMonsters(int amount) {
		amount = (amount / 2)+1;
		amount = random.nextInt(amount)+1;
		List<String> new_monsters = new ArrayList<String>();
		if (rooms.size() > 1) {
			for (; amount > 0; amount--) {
				Room r = rooms.get(getRoomNum());
				Monster m = getMonster();
				r.monsters.add(m);
				new_monsters.add(m.name);
			}
			return new_monsters;
		}
		return null;
	}
	
	private Monster getMonster() {
		String name = monster_names.get(random.nextInt(monster_names.size()-1));
		monster_names.remove(name);
		boolean agg = random.nextBoolean();
		int health = random.nextInt(monster_health)+monster_health;
		Monster m = new Monster(name, agg, health);
		
		int ms = random.nextInt(monster_weapons+1)+1;
		for (; ms > 0; ms--) m.weapons.add(getWeapon());
		
		ms = random.nextInt(monster_armor+1);
		for (; ms > 0; ms--) m.armor.add(getArmor());
		
		return m;
	}
	
	private void setItems(Room r) {
		int ws = random.nextInt(room_weapons+1);
		int as = random.nextInt(room_armor+1);
		int fs = random.nextInt(room_food+1);
		for (; ws > 0; ws--)
			r.weapons.add(getWeapon());
		for (; as > 0; as--)
			r.armor.add(getArmor());
		for (; fs > 0; fs--)
			r.food.add(getFood());
	}
	
	private String getRoomName() {
		String type = room_types.get(random.nextInt(room_types.size()-1));
		String name = room_names.get(random.nextInt(room_names.size()-1));
		room_names.remove(name);
		return cap(type) + " of " + cap(name);
	}
	
	private Weapon getWeapon() {
		String[] is = weapons.get(random.nextInt(weapons.size()-1));
		Weapon w = new Weapon(is[0], Integer.parseInt(is[1]));
		return w;
	}
	
	public Weapon getBestWeapon() {
		int h = 0;
		String[] is = null;
		for (String[] s: weapons) {
			if (Integer.parseInt(s[1]) > h) {
				h = Integer.parseInt(s[1]);
				is = s;
			}
		}
		Weapon w = new Weapon(is[0], Integer.parseInt(is[1]));
		return w;
	}
	
	private Weapon getPuniestWeapon() {
		int h = 100;
		String[] is = null;
		for (String[] s: weapons) {
			if (Integer.parseInt(s[1]) < h) {
				h = Integer.parseInt(s[1]);
				is = s;
			}
		}
		Weapon w = new Weapon(is[0], Integer.parseInt(is[1]));
		return w;
	}
	
	public Armor getStrongestArmor() {
		int h = 0;
		String[] is = null;
		for (String[] s: armor) {
			if (Integer.parseInt(s[1]) > h) {
				h = Integer.parseInt(s[1]);
				is = s;
			}
		}
		Armor a = new Armor(is[0], Integer.parseInt(is[1]));
		return a;
	}
	
	private Armor getArmor() {
		String[] is = armor.get(random.nextInt(armor.size()-1));
		Armor a = new Armor(is[0], Integer.parseInt(is[1]));
		return a;
	}
	
	private Food getFood() {
		String[] is = food.get(random.nextInt(food.size()-1));
		Food f = new Food(is[0], Integer.parseInt(is[1]), random.nextInt(99)+1);
		return f;
	}

	
	private int getFullRoomNum() {
		int r = random.nextInt(rooms.size());
		return squeeze(r, 0, rooms.size()-1);
	}

	private int getRoomNum() {
		int r = random.nextInt(rooms.size());
		return squeeze(r, 1, rooms.size()-1);
	}
	
	private int squeeze(int input, int lower, int upper) {
		if (upper < lower) upper = lower;
		if (input < lower) return lower;
		if (input > upper) return upper;
		return input;
	}
	
	private String cap(String s) {
		return s.substring(0,1).toUpperCase()+s.substring(1);
	}
	
	
	private List<String[]> splitLines(List<String> lines) throws Exception {
		List<String[]> res = new ArrayList<String[]>();
		for (String line: lines) {
			res.add(line.split(","));
		}
		return res;
	}
	
	private List<String> readLines(String filename) throws Exception {
		br = new BufferedReader(new FileReader(filename));
		ArrayList<String> res = new ArrayList<String>();
		
		String line;
		while ((line = br.readLine()) != null) 
			res.add(line);
		
		br.close();
		return res;
	}

}
