// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.util.*;

class Inventory {
	
	public ArrayList<Weapon> weapons = new ArrayList<Weapon>();
	public ArrayList<Armor> armor = new ArrayList<Armor>();
	public ArrayList<Food> food = new ArrayList<Food>();


	public List<String[]> strWeapons() {
		List<String[]> str = new ArrayList<String[]>();
		for (Weapon weapon: weapons) {
			String[] s = { weapon.name, weapon.hitpoints+"" };
			str.add(s);
		}
		return str;
	}
	
	public List<String[]> strArmor() {
		List<String[]> str = new ArrayList<String[]>();
		for (Armor ar: armor) {
			String[] s = { ar.name, ar.strength+"" };
			str.add(s);
		}
		return str;
	}
	
	public List<String[]> strFood() {
		List<String[]> str = new ArrayList<String[]>();
		for (Food f: food) {
			String[] s = { f.name, String.format("%3s", f.getValue()) };
			str.add(s);
		}
		return str;
	}
	
	public Weapon getBestWeapon() {
		int h = 0;
		Weapon wep = null;
		for (Weapon w: weapons) {
			if (w.hitpoints > h) {
				h = w.hitpoints;
				wep = w;
			}
		}
		return wep;
	}
	
	public int getBestWeaponValue() {
		if (getBestWeapon() == null) return 0;
		return getBestWeapon().hitpoints;
	}
	
	public Weapon getPuniestWeapon() {
		int h = 100;
		Weapon wep = null;
		for (Weapon w: weapons) {
			if (w.hitpoints < h) {
				h = w.hitpoints;
				wep = w;
			}
		}
		return wep;
	}
	
	public Armor getStrongestArmor() {
		int s = 0;
		Armor ar = null;
		for (Armor a: armor) {
			if (a.strength > s) {
				s = a.strength;
				ar = a;
			}
		}
		return ar;
	}
	
	public int getStrongestArmorValue() {
		if (getStrongestArmor() == null) return 0;
		return getStrongestArmor().strength;
	}
	
	public Armor getWeakestArmor() {
		int s = 100;
		Armor ar = null;
		for (Armor a: armor) {
			if (a.strength < s) {
				s = a.strength;
				ar = a;
			}
		}
		return ar;
	}

	public Food getLeastFood() {
		double l = 100.0;
		Food fo = null;
		for (Food f: food) {
			if (f.getValue() < l) {
				l = f.getValue();
				fo = f;
			}
		}
		return fo;
	}
	
}