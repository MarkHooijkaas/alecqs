package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;

public class MacroCommand extends NamedCommand {
	public MacroCommand(String name) { super(name); }

	@Override protected void execute(Parser parser, LineSource src, String remainder) {
		String name=remainder.trim();
		StringBuilder macro=new StringBuilder();
		String line;
		while ((line = src.getLine()) != null) {
			if (line.trim().startsWith("@ENDMACRO"))
				break;
			else
				macro.append(line).append("\n");
		}
		parser.setProp(name, macro.toString());
	}
}
