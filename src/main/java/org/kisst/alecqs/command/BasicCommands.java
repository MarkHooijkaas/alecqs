package org.kisst.alecqs.command;

import org.kisst.alecqs.linesource.FileSource;
import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.StringSource;
import org.kisst.alecqs.logger.Logger;

import java.io.File;

public class BasicCommands {
	public final static Command LOAD = new NamedCommand("LOAD") {
		@Override protected void execute(Parser parser, String remainder) {
			File f = new File(parser.getDir(), remainder.trim());
			parser.logger.logDebug("Loading: " + f);
			FileSource fs = new FileSource(f);
			Parser p = new Parser.Builder(parser)
					.src(fs)
					.logger(parser.logger.createChildLogger(fs))
					.dir(fs.getDir())
					.build();
			p.setLocalProp("FILENAME", fs.getFilename());
			p.setLocalProp("FILEBASE", fs.getFilebase());
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
			String macro=parser.getProp(cmd);
			if (macro==null)
				throw new RuntimeException("could not find macro "+cmd);
			StringSource src= new StringSource("MACRO "+cmd, macro);
			Parser p = new Parser.Builder(parser)
					.src(src)
					.logger(parser.logger.createChildLogger(src))
					.build();
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
