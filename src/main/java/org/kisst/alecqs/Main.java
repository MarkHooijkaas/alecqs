package org.kisst.alecqs;

import org.kisst.alecqs.command.BasicCommands;
import org.kisst.alecqs.linesource.StringSource;
import org.kisst.alecqs.logger.Logger;
import org.kisst.alecqs.logger.SimpleSourceLogger;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String args[]) throws IOException {

		for (int i=0; i<args.length; i++) {
			if (args[i].indexOf('=')>0)
				args[i]="@GLOBAL "+args[i];
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
		//Parser parser=new Parser(null, argsrc, dir);
		Parser parser= new Parser.Builder()
				.logger(new SimpleSourceLogger(argsrc,""))
				.commands(BasicCommands.all)
				.build();
		parser.parse(argsrc);
		parser.closeOutput();
	}
}
