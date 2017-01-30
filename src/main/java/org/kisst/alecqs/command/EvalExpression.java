package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EvalExpression {

	private static ScriptEngine engine =null;

	public static Object eval(Parser parser, String expr) {
		expr=parser.substitute(expr);
		if (engine==null) {
			ScriptEngineManager manager = new ScriptEngineManager();
			engine = manager.getEngineByName("JavaScript");
		}

		try {
			return engine.eval(expr, new ParserBinding(parser));
		} catch (ScriptException e) { throw new RuntimeException(e); }
	}

	private static class ParserBinding implements Bindings {
		private final Parser parser;

		private ParserBinding(Parser parser) {
			this.parser = parser;
		}

		@Override
		public Object put(String s, Object o) {
			//throw new RuntimeException("Not Implemented: put("+s+","+o+")");
			return o;
		}

		@Override
		public void putAll(Map<? extends String, ?> map) {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public void clear() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public Set<String> keySet() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public Collection<Object> values() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public int size() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public boolean isEmpty() {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public boolean containsKey(Object o) {
			return parser.getProp(o.toString())!=null;
		}

		@Override
		public boolean containsValue(Object o) {
			throw new RuntimeException("Not Implemented");
		}

		@Override
		public Object get(Object o) {
			return parser.getProp(o.toString());
		}

		@Override
		public Object remove(Object o) {
			throw new RuntimeException("Not Implemented");
		}
	}
}
