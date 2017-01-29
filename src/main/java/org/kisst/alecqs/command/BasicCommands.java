package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;
import org.kisst.alecqs.logger.Logger;

import java.io.File;

public class BasicCommands {

	public final static Command RUN = new RunCommand("RUN");
	public final static Command LOAD = new LoadCommand("LOAD");
	public final static Command MACRO = new MacroCommand("MACRO");
	public final static Command CALC = new CalcCommand("CALC");
	public final static Command IF = new IfCommand();

	public final static Command GLOBAL = new NamedCommand("GLOBAL") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			parser.getRoot().parseLine(src, parser.substitute(remainder.trim()));
		}
	};

	public final static Command PARENT = new NamedCommand("PARENT") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			parser.getParent().parseLine(src, parser.substitute(remainder.trim()));
		}
	};

	public final static Command VAR = new NamedCommand("VAR") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			parser.parseProp(parser.substitute(remainder.trim()));
		}
	};

	public final static Command DEFAULT = new NamedCommand("DEFAULT") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			int pos= remainder.indexOf("=");
			if (pos<=0)
				throw new RuntimeException("Syntax error: @DEFAULT <var>=<text>");
			String key=remainder.substring(0,pos).trim();
			if (parser.getProp(key)==null)
				parser.parseProp(parser.substitute(remainder.trim()));
		}
	};

	public final static Command LOGLEVEL = new NamedCommand("LOGLEVEL") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			parser.logger.setLogLevel(Logger.Level.valueOf(remainder.trim()));
		}
	};

	public final static Command OUTPUTFILE = new NamedCommand("OUTPUTFILE") {
		@Override protected void execute(Parser parser, LineSource src, String remainder) {
			File f = new File(parser.getDir(), parser.substitute(remainder.trim()));
			parser.changeOutputFile(f);
		}
	};


	public final static CommandList all = new CommandList(
			LOAD,
			GLOBAL,
			RUN,
			MACRO,
			CALC,
			IF,
			VAR,
			DEFAULT,
			PARENT,
			OUTPUTFILE,
			LOGLEVEL
	);
}
