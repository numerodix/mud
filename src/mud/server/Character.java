// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import java.util.regex.*;

abstract class Character extends Inventory {

	public String name;
	
	public Combat combat = null;
	
	protected int health;
	
	protected int weapon_capacity = 3;
	protected int armor_capacity = 3;
	protected int food_capacity = 3;
	
	public Item pickup(Item item, Room room) {
		Item res = null;
		if (item instanceof Weapon) {
			Weapon w = (Weapon) item;
			if (weapons.size() >= weapon_capacity) {
				Weapon wpuny = getPuniestWeapon();
				res = wpuny;
				weapons.remove(wpuny);
				room.weapons.add(wpuny);
			}
			weapons.add(w);
			room.weapons.remove(w);
		}
		if (item instanceof Armor) {
			Armor a = (Armor) item;
			if (armor.size() >= armor_capacity) {
				Armor aweak = getWeakestArmor();
				res = aweak;
				armor.remove(aweak);
				room.armor.add(aweak);
			}
			armor.add(a);
			room.armor.remove(a);
		}
		if (item instanceof Food) {
			Food f = (Food) item;
			if (food.size() >= food_capacity) {
				Food fleast = getLeastFood();
				res = fleast;
				food.remove(fleast);
				room.food.add(fleast);
			}
			food.add(f);
			room.food.remove(f);
		}
		return res;
	}
	
	public int getHealth() { return health; }
	
	public void incHealth(int amount) {
		if (health + amount > 100) health = 100;
		else health += amount;
	}
	
	public void decHealth(int amount) {
		if (health - amount < 0) health = 0;
		else health -= amount;
	}
	
	public int getFerocity() {
		return getStrongestArmorValue() + health;
	}

}

class Human extends Character {
	
	public User user;
	

	public Human(User user) {
		this.user = user;
		this.name = user.username;
		health = 50;
	}
	
	
	public Food findFood(String food_regex) {
		for (Food f: food) {
			Matcher matcher = Pattern.compile(food_regex).matcher(f.name);
			if (matcher.find()) {
				return f;
			}
		}
		return null;
	}

	public boolean consume(Food f) {
		if (f.getValue() + health > 100)
			return false;
		food.remove(f);
		health += f.getValue();
		return true;
	}
	
}

class Monster extends Character {

	public boolean aggressive;
	
	public Monster(String name, boolean aggressive, int health) {
		this.name = name;
		this.aggressive = aggressive;
		this.health = health;
	}
	
}