package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.StringSource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CalcCommand extends NamedCommand {
	public CalcCommand(String name) { super(name); }

	private static ScriptEngineManager manager = null;
	private static ScriptEngine engine =null;

	@Override protected void execute(Parser parser, String remainder) {
		remainder=remainder.trim();
		int pos=remainder.indexOf('=');
		if (pos<=0)
			throw new RuntimeException("Syntax error @CALC <var>=<expr>");
		String varname=remainder.substring(0,pos).trim();
		String expr=parser.substitute(remainder.substring(pos+1));

		if (engine==null) {
			manager = new ScriptEngineManager();
			engine = manager.getEngineByName("JavaScript");
		}


		try {
			Object result = engine.eval(expr);
			if (result!=null)
				parser.setProp(varname,result.toString());
			// TODO: set null?
		} catch (ScriptException e) { throw new RuntimeException(e); }
	}
}
