package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;
import org.kisst.alecqs.linesource.StringSource;

public class RunCommand extends NamedCommand {
	public RunCommand(String name) { super(name); }

	@Override protected void execute(Parser parser, LineSource src, String remainder) {
		remainder=remainder.trim();
		String cmd=getFirstToken(remainder);
		remainder=remainder.substring(cmd.length());
		String macro=parser.getProp(cmd);
		if (macro==null)
			throw new RuntimeException("could not find macro "+cmd);
		StringSource stringSrc= new StringSource("MACRO "+cmd, macro);
		Parser p = new Parser.Builder(parser)
				.logger(parser.logger.createChildLogger(src))
				.build();
		String[] args=remainder.split("[,]+");
		for (String arg: args) {
			arg=arg.trim();
			if (arg.length()>0)
				p.parseProp(parser.substitute(arg));
		}
		p.parse(stringSrc);

	}
}
