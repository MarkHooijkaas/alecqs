package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class EvalExpression {

	private static ScriptEngine engine =null;

	public static Object eval(Parser parser, String expr) {
		expr=parser.substitute(expr);
		if (engine==null) {
			ScriptEngineManager manager = new ScriptEngineManager();
			engine = manager.getEngineByName("JavaScript");
		}

		try {
			return engine.eval(expr);
		} catch (ScriptException e) { throw new RuntimeException(e); }
	}
}
