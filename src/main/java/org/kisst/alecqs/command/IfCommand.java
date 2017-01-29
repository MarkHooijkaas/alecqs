package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.LineSource;

public class IfCommand extends NamedCommand {
	public IfCommand() { super("IF"); }

	@Override protected void execute(Parser parser, LineSource src, String remainder) {
		String expr=remainder.trim();
		Object value=EvalExpression.eval(parser, expr);
		boolean active= value != null && Boolean.parseBoolean(value.toString());
		String line;
		while ((line = src.getLine()) != null) {
			if (line.trim().startsWith("@ENDIF"))
				break;
			else if (line.trim().startsWith("@ELSE"))
				active=! active;
			else if (active)
				parser.parseLine(src,line);
			else {
				// ignore line, but
				// TODO: count nested IF's
			}
		}
	}
}
