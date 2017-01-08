package org.kisst.alecqs.logger;

import org.kisst.alecqs.Parser;

public class ParserLogger extends BaseLogger {
	private final Parser parser;
	private final String prefix;
	private final String indent;
	private Level currentLogLevel;

	public ParserLogger(Parser parser) { this(parser, "");}
	public ParserLogger(Parser parser, String prefix) {
		this.parser=parser;
		if (parser.parent==null) {
			setLogLevel(Level.WARN);
			this.indent="";
		}
		else {
			setLogLevel(parser.parent.logger.getLogLevel());
			if (parser.parent.logger instanceof ParserLogger)
				this.indent=((ParserLogger) parser.parent.logger).indent+"\t";
			else
				this.indent="";
		}
		this.prefix=prefix=parser.src.getName()+":";
	}

	@Override public void setLogLevel(Level level) { this.currentLogLevel=level; }
	@Override public Level getLogLevel() { return currentLogLevel;}
	@Override public boolean levelEnabled(Level level) { return level.value<= currentLogLevel.value;}
	@Override public void log(Level level, String s) {
		if (levelEnabled(level))
			System.out.println(indent+prefix+parser.src.getLinenr()+": "+s);
	}
}
