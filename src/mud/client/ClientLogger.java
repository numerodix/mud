// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.client;

import org.apache.log4j.Level;

import mud.common.*;

class ClientLogger extends Logger {

	public ClientLogger() {
		super("Client", Level.WARN);
	}

}