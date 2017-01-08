package org.kisst.alecqs.logger;

public interface Logger {
	public enum Level {
		NONE(0),
		FATAL(10),
		ERROR(20),
		WARN(30),
		INFO(40),
		DEBUG(50),
		TRACE(60);
		public final int value;
		Level(int value) { this.value = value;}
	}

	public void setLogLevel(Level level);
	public Level getLogLevel();

	public boolean levelEnabled(Level level);
	public boolean fatalEnabled();
	public boolean errorEnabled();
	public boolean warnEnabled();
	public boolean infoEnabled();
	public boolean debugEnabled();
	public boolean traceEnabled();

	public void log(Level level, String s);
	public void logFatal(String s);
	public void logError(String s);
	public void logWarn(String s);
	public void logInfo(String s);
	public void logDebug(String s);
	public void logTrace(String s);

}
