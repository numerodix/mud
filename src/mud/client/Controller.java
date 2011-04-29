// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.client;

public interface Controller {
	
	void broadcast(String msg);
	void update(String msg);
	void notify(String msg);
	
	void enteredRoom(String room_name);
	void underAttack(String character);
	void struck(String character, int damage, int health);
	void wasStruck(String character, int damage, int health);
	void killed(String character);
	void wasKilled(String character);
	void attackStopped(String character);
	
}