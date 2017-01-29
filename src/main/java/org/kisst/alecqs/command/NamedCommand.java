package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;

public abstract class NamedCommand implements Command {
	private final String name;

	public NamedCommand(String name) { this.name="@"+name;}

	@Override public boolean handle(Parser parser, LineSource src, String line) {
		if (! line.trim().startsWith(name))
			return false;
		String remainder=line.substring(name.length());
		parser.logger.logInfo("Command: "+name+remainder);
		execute(parser, src, remainder);
		return true;
	}

	protected abstract void execute(Parser parser, LineSource src, String remainder);
	@Override public String toString() { return "Command("+name+")"; }

	public static String getFirstToken(String line) {
		line=line.trim();
		int pos1=line.indexOf(' ');
		int pos2=line.indexOf('\t');
		if (pos2>0 && pos2<pos1)
			pos1=pos2;
		if (pos1>0)
			return line.substring(0,pos1);
		return line;
	}
}
