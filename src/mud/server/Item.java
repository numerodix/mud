// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

abstract class Item {
	
	public String name;
	
}

class Weapon extends Item {

	public int hitpoints;
	
	public Weapon(String name, int hitpoints) {
		this.name = name;
		this.hitpoints = hitpoints;
	}
	
}

class Armor extends Item {

	public int strength;
	
	public Armor(String name, int strength) {
		this.name = name;
		this.strength = strength;
	}

}

class Food extends Item {

	public int nutrition;
	public int amount;

	public Food(String name, int nutrition, int amount) {
		this.name = name;
		this.nutrition = nutrition;
		this.amount = amount;
	}
	
	public int getValue() {
		return (int) ((nutrition/100.0) * amount);
	}
	
}
