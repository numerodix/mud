// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import org.apache.log4j.Level;

import mud.common.*;

class ServerLogger extends Logger {
	
	public ServerLogger() {
		super("Server", Level.WARN);
	}

}