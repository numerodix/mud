// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.server;

import mud.common.*;

class GameTicker extends Thread {
	
	private GameImpl game;
	public Logger log;
//	private long tick = 1000L;
	private long tick = 15000L;
	
	// interval in ticks
	final private int heal_interval = 7;
	final private int monster_interval = 3;
	
	
	public GameTicker(GameImpl game, Logger log) {
		this.game = game;
		this.log = log;
		
		this.setName("GameTicker");
		this.start();
	}
	
	public void run() {
		int healer = 0;
		
		while (true) {
			try {
//				log.debug("GameTicker wake-up");
				game.invokeRespawnUsers();
				
//				log.debug("GameTicker run room adjustment");
				game.invokeRoomAdjustment();
				
				if (healer % monster_interval == 0) {
	//				log.debug("GameTicker run monsters adjustment");
					game.invokeMonsterAdjustment();
				}
				
//				log.debug(healer+" "+heal_interval);
				if (healer % heal_interval == 0) {
//					log.debug("GameTicker run heal humans");
					game.invokeHealHumans();
				}
				
//				log.debug("GameTicker sleep");
				Thread.sleep(tick);
				healer++;
			} catch (InterruptedException e) {
				interrupt();
			}
		}
	}

}