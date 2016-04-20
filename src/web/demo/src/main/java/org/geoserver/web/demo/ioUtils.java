package org.geoserver.web.demo;

import org.apache.commons.lang.SystemUtils;
import org.springframework.security.web.header.writers.frameoptions.StaticAllowFromStrategy;

public class ioUtils {
	
	public static final String MIME_TYPE = "application/octet-stream";
	
	public static String getCanonicalPath(String absPath) {
		String f = null;
		if (!SystemUtils.IS_OS_WINDOWS) {
			f = "file:/";
		} else {
			f = "file://";
		}
		
		
		return null;
	}

}
