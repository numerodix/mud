// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.common;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Config {
	
	public static enum Platform { UNIX, OTHER }

	public static String revision_file = "revision";
	public static String buildnum_file = "build.number";
	
	public static String date = "$Date$";
	public static String revision = "$Rev$";
	
	
	public static Platform os() {
		if ("Linux".equals(System.getProperty("os.name")))
			return Platform.UNIX;
		return null;
	}
	
	public static String version() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(revision_file));
		
			String line; String out = null;
			Pattern pattern = Pattern.compile("revision=([a-zA-Z0-9]+)");
			while ((line = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					out = matcher.group(1);
				}
			}
			
			br.close();
			return "revision "+out;
		} catch (Exception e) { return null; }
	}
	
	public static String build() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(buildnum_file));
		
			String line; String out = null;
			Pattern pattern = Pattern.compile("build.number=([0-9]+)");
			while ((line = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					out = matcher.group(1);
				}
			}
			
			br.close();
			return out;
		} catch (Exception e) { return null; }
	}
}
