package org.kisst.alecqs;

import java.io.File;

public class BasicCommands {
	public final static Command LOAD = new NamedCommand("LOAD") {
		@Override protected void execute(Parser parser, String remainder) {
			File f = new File(parser.getDir(), remainder.trim());
			parser.log(Parser.INFO, "Loading: " + f);
			FileSource fs = new FileSource(f);
			Parser p = new Parser(parser, fs);
			p.parse();

		}
	};

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


	public final static Command MACRO = new NamedCommand("MACRO") {
		@Override protected void execute(Parser parser, String remainder) {
			String name=remainder.trim();
			StringBuilder macro=new StringBuilder();
			String line;
			while ((line = parser.readLine()) != null) {
				if (line.trim().startsWith("@ENDMACRO"))
					break;
				else
					macro.append(line).append("\n");
			}
			parser.getRoot().setLocalProp(name, macro.toString());
		}
	};

	public final static Command RUN = new NamedCommand("RUN") {
		@Override protected void execute(Parser parser, String remainder) {
			remainder=remainder.trim();
			String cmd=getFirstToken(remainder);
			remainder=remainder.substring(cmd.length());
			parser.log(Parser.INFO, "Running macro: "+cmd);
			String macro=parser.getProp(cmd);
			if (macro==null)
				throw new RuntimeException("could not find macro "+cmd);
			StringSource lines= new StringSource("MACRO "+cmd, macro);
			Parser p=new Parser(parser, lines, parser.getDir());
			String[] args=remainder.split("[,]+");
			for (String arg: args) {
				arg=arg.trim();
				if (arg.length()>0)
					p.parseLocalProp(parser.substitute(arg));
			}
			p.parse();

		}
	};

	public final static Command VAR = new NamedCommand("VAR") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.parseLocalProp(parser.substitute(remainder));
		}
	};

	public final static Command LOGLEVEL = new NamedCommand("LOGLEVEL") {
		@Override protected void execute(Parser parser, String remainder) {
			parser.currentLogLevel =Integer.parseInt(remainder.trim());
		}
	};

	public final static Command OUTPUTFILE = new NamedCommand("OUTPUTFILE") {
		@Override protected void execute(Parser parser, String remainder) {
			File f = new File(parser.getDir(), parser.substitute(remainder.trim()));
			parser.changeOutputFile(f);
		}
	};


	public final static Command[] all = new Command[]{
			LOAD,
			GLOBAL,
			RUN,
			MACRO,
			VAR,
			PARENT,
			OUTPUTFILE,
			LOGLEVEL
	};
}
