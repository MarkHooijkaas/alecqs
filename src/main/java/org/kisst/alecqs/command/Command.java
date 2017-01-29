package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;

public interface Command {
	/**
	 * @return  true if it this line is handled by this command, false if another command can handle it
	 *
	 */
	public boolean handle(Parser parser, LineSource src, String line);
}
