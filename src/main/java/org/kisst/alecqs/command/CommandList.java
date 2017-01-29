package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;

public  class CommandList implements Command {
	private final Command[] cmds;

	public CommandList(Command... cmds) { this.cmds=cmds;}

	@Override public boolean handle(Parser parser, LineSource src, String line) {
		for (Command cmd: cmds) {
			if (cmd.handle(parser, src,  line))
				return true;
		}
		return false;
	}
}
