// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.client;

public class Client {

	static public void main(String args[]) {

		String naming_host = "localhost";
		if (args.length > 0) naming_host = args[0];
	
		try {
			new ClientCmdLine(naming_host);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}