// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.
//
// <desc> A mud game </desc>

package mud.server;

public class Server {

	static public void main(String args[]) {
		try {
			new ServerCmdLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
