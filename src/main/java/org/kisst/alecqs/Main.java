package org.kisst.alecqs;

import org.kisst.alecqs.logger.Logger;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String args[]) throws IOException {

		for (int i=0; i<args.length; i++) {
			if (args[i].indexOf('=')>0)
				continue;
			else if (args[i].equals("-v"))
				args[i]="@LOGLEVEL "+ Logger.Level.INFO;
			else if (args[i].equals("-d"))
				args[i]="@LOGLEVEL "+Logger.Level.DEBUG;
			else if (args[i].equals("-q"))
				args[i]="@LOGLEVEL "+Logger.Level.NONE;
			else
				args[i]="@LOAD "+args[i];
		}
		File dir=new File(".");
		StringSource argsrc = new StringSource("main", args);
		Parser parser=new Parser(null, argsrc, dir);
		parser.parse();
		parser.closeOutput();
	}
}
