package org.cbrain.util;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogPattern {
	 public static String propertyFile="log4j.properties";//默认当前位置
	 private static LogPattern pattern;
		private LogPattern() {
			PropertyConfigurator.configure(propertyFile);
		}

		public static Logger getInstance(String name) {
			if(pattern==null){
				pattern=new LogPattern();
			}
			return  Logger.getLogger(LogPattern.class.getName());
		}

		public static void main(String[] args) throws IOException, SQLException {
			Logger log=	LogPattern.getInstance(LogPattern.class.getName());
			log.error("Hello this is an error message");
			log.info("Hello this is an info message");
			log.debug("test debug");
		}

}
