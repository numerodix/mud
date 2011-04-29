// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.common;

//import org.apache.log4j.*;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

abstract public class Logger {
	
	private org.apache.log4j.Logger log;
	
	private Layout layout;
	private Appender appender;
	
	
	public Logger(String location, Level level) {
		layout = new PatternLayout("["+location+" %d{HH:mm:ss}] %-5p [%t]: %m%n");
		appender = new ConsoleAppender(layout);
		log = org.apache.log4j.Logger.getLogger(location);
		log.setLevel(level);
		log.addAppender(appender);
	}
	
	public void trace(String msg) {
		log.trace(trim(msg));
	}

	public void debug(String msg) {
		log.debug(trim(msg));
	}

	public void info(String msg) {
		log.info(trim(msg));
	}
	
	public void warn(String msg) {
		log.warn(trim(msg));
	}

	public void error(String msg) {
		log.error(trim(msg));
	}

	public void fatal(String msg) {
		log.fatal(trim(msg));
	}
	
	
	private String trim(String msg) {
		return msg.replaceAll("\n","");
	}

}
