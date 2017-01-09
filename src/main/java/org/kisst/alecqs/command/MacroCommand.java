package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;

public class MacroCommand extends NamedCommand {
	public MacroCommand(String name) { super(name); }

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
}
