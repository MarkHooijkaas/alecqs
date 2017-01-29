package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;



public class CalcCommand extends NamedCommand {
	public CalcCommand(String name) { super(name); }


	@Override protected void execute(Parser parser, String remainder) {
		remainder=remainder.trim();
		int pos=remainder.indexOf('=');
		if (pos<=0)
			throw new RuntimeException("Syntax error @CALC <var>=<expr>");
		String varname=remainder.substring(0,pos).trim();
		String expr=remainder.substring(pos+1);

		Object result = EvalExpression.eval(parser, expr);
		if (result!=null)
			parser.setProp(varname,result.toString());
		else
			parser.setProp(varname, null); // TODO: is this desired?
	}
}
