package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.logger.Logger;

import java.io.File;

public class BasicCommands {

	public final static Command RUN = new RunCommand("RUN");
	public final static Command LOAD = new LoadCommand("LOAD");
	public final static Command MACRO = new MacroCommand("MACRO");

	public final static Command GLOBAL = new NamedCommand("GLOBAL") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.getRoot().parseLine(parser.substitute(remainder));
		}
	};

	public final static Command PARENT = new NamedCommand("PARENT") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.getParent().parseLine(parser.substitute(remainder.trim()));
		}
	};

	public final static Command VAR = new NamedCommand("VAR") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.parseLocalProp(parser.substitute(remainder));
		}
	};

	public final static Command LOGLEVEL = new NamedCommand("LOGLEVEL") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.logger.setLogLevel(Logger.Level.valueOf(remainder.trim()));
		}
	};

	public final static Command OUTPUTFILE = new NamedCommand("OUTPUTFILE") {
		@Override protected void execute(Parser parser, String remainder) {
			File f = new File(parser.getDir(), parser.substitute(remainder.trim()));
			parser.changeOutputFile(f);
		}
	};


	public final static CommandList all = new CommandList(
			LOAD,
			GLOBAL,
			RUN,
			MACRO,
			VAR,
			PARENT,
			OUTPUTFILE,
			LOGLEVEL
	);
}
