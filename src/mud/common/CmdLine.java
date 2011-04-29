// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.common;

import java.io.*;
import java.util.*;
import java.util.regex.*;

abstract public class CmdLine {
	
	protected Logger log;
	
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
	private Hashtable<String,String> colors;
	
	public String color_header = "blue";
	public String color_prompt = "green";
	public String color_instructions = "yellow";
	public String color_output = "white";
	public String color_alarm = "red";
	public String color_combat_give = "dmagenta";
	public String color_combat_receive = "magenta";
	public String color_alarm_off = "dred";
	public String color_error = "red";
	
	public String color_broadcast = "dyellow";
	public String color_update = "dcyan";
	public String color_notification = "cyan";
	

	public CmdLine() {
		colors = new Hashtable<String,String>();
		colors.put("black", "\033[1;30m");
		colors.put("red", "\033[1;31m");
		colors.put("green", "\033[1;32m");
		colors.put("yellow", "\033[1;33m");
		colors.put("blue", "\033[1;34m");
		colors.put("magenta", "\033[1;35m");
		colors.put("cyan", "\033[1;36m");
		colors.put("white", "\033[1;37m");
		
		colors.put("reset", "\033[m");
		
		colors.put("dblack", "\033[0;30m");
		colors.put("dred", "\033[0;31m");
		colors.put("dgreen", "\033[0;32m");
		colors.put("dyellow", "\033[0;33m");
		colors.put("dblue", "\033[0;34m");
		colors.put("dmagenta", "\033[0;35m");
		colors.put("dcyan", "\033[0;36m");
		colors.put("dwhite", "\033[0;37m");
		if (Config.os() != Config.Platform.UNIX) {
			for (String key: colors.keySet()) colors.put(key, "");
		}
	}
	
	
	abstract protected void displayPrompt();
	
	abstract protected void acceptCommand();
	
	protected String readInput() {
		try {
			String input = br.readLine();
			return input;
		} catch (IOException e) { log.warn(e.getMessage()); }
		return null;
	}
	
	protected void displayHelp(String[][] usage) {
		for (String[] line: usage) {
			ins(String.format("%-16s  %s", line[0], line[1]));
		}
	}
	
	protected void displayStack() {
		(new Exception()).printStackTrace();
	}
	
	protected void putcs(String s) {
		output(s, color_output);
	}

	protected void putc(String s) {
		outputn(s, color_output);
	}
	
	protected void puts(String s) {
		puts(s, color_output);
	}
	
	protected void puts(String s, String color) {
		output(" "+s, color);
	}
	
	protected void put(String s) {
		put(s, color_output);
	}
	
	protected void put(String s, String color) {
		outputn(" "+s, color);
	}
	
	protected void ins(String s) {
		output(" "+s, color_instructions);
	}
	
	protected void pro(String s) {
		outputn(s, color_prompt);
	}
	
	protected void not(String s) {
		output(">>> "+s, color_notification);
	}
	
	protected void bro(String s) {
		output(">>> "+s, color_broadcast);
	}
	
	protected void upd(String s) {
		output(">>> "+s, color_update);
	}
	
	protected void ala(String s) {
		output(">>> "+s, color_alarm);
	}
	
	protected void alo(String s) {
		output(">>> "+s, color_alarm_off);
	}
	
	protected void com(String s) {
		output(">>> "+s, color_combat_give);
	}
	
	protected void cor(String s) {
		output(">>> "+s, color_combat_receive);
	}
	
	protected void err(String s) {
		puts(s, color_error);
	}
	
	
	private void output(String s, String color) {
		System.out.println(colors.get(color)+s+colors.get("reset"));
	}
	
	private void outputn(String s, String color) {
		System.out.print(colors.get(color)+s+colors.get("reset"));
	}
	

	protected void putItems(List<String[]> is, String[] labels, String fmt) {
		if (is.size() != 0) {
			for (int i=0; i < labels.length; i++) labels[i] = "{"+labels[i]+"}";
			puts(String.format(fmt, (Object[]) labels), color_header);
			for (String[] i: is) {
				puts(String.format(fmt, (Object[]) i));
			}
		}
	}
	
	protected String match1args(String c, String s) {
		Pattern pattern = Pattern.compile(c+" +(\\w+) *");
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			String res = matcher.group(1);
			return res;
		}
		return null;
	}
	
	protected String[] match2args(String c, String s) {
		Pattern pattern = Pattern.compile(c+" +(\\w+) +(\\w+) *");
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			String res[] = new String[2];
			res[0] = matcher.group(1);
			res[1] = matcher.group(2);
			return res;
		}
		return null;
	}
	
	protected String version() {
		return Config.version()+" [build "+Config.build()+"]";
	}
	
}
