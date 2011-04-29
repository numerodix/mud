// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import mud.interfaces.*;

class User {

	public String username;
	public String password;
	
	public Player player;
	public String last_host;
	
	public Human human;

	public User(String username, String password, Player player, String host) {
		this.username = username;
		this.password = password;
		this.player = player;
		this.last_host = host;
	}
	
}