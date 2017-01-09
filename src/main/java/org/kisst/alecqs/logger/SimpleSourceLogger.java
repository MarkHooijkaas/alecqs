package org.kisst.alecqs.logger;

import org.kisst.alecqs.linesource.LineSource;

public class SimpleSourceLogger extends BaseLogger implements SourceLogger {
	private final LineSource src;
	private final String prefix;
	private final String indent;
	private Level currentLogLevel=Level.WARN;

	public SimpleSourceLogger(LineSource src, String indent) {
		this.src=src;
		this.indent=indent;
		this.prefix=src.getName()+":";
	}

	@Override public SourceLogger createChildLogger(LineSource src) {
		SimpleSourceLogger result = new SimpleSourceLogger(src, indent + "\t");
		result.setLogLevel(currentLogLevel);
		return result;
	}
	@Override public void setLogLevel(Level level) { this.currentLogLevel=level; }
	@Override public Level getLogLevel() { return currentLogLevel;}
	@Override public boolean levelEnabled(Level level) { return level.value<= currentLogLevel.value;}
	@Override public void log(Level level, String s) {
		if (levelEnabled(level))
			System.out.println(indent+prefix+src.getLinenr()+": "+s);
	}
}
