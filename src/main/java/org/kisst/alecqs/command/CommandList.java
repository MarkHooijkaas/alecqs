package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;

public  class CommandList implements Command {
	private final Command[] cmds;

	public CommandList(Command... cmds) { this.cmds=cmds;}

	@Override public boolean handle(Parser parser, String line) {
		for (Command cmd: cmds) {
			if (cmd.handle(parser, line))
				return true;
		}
		return false;
	}
}
