package org.kisst.alecqs.logger;

public abstract class BaseLogger implements Logger {

	@Override public boolean fatalEnabled() {return levelEnabled(Level.FATAL);}
	@Override public boolean errorEnabled() {return levelEnabled(Level.ERROR);}
	@Override public boolean warnEnabled()  {return levelEnabled(Level.WARN);}
	@Override public boolean infoEnabled()  {return levelEnabled(Level.INFO);}
	@Override public boolean debugEnabled() {return levelEnabled(Level.DEBUG);}
	@Override public boolean traceEnabled() {return levelEnabled(Level.TRACE);}

	@Override public void logFatal(String s) { log(Level.FATAL,s); }
	@Override public void logError(String s) { log(Level.ERROR,s); }
	@Override public void logWarn(String s)  { log(Level.WARN,s); }
	@Override public void logInfo(String s)  { log(Level.INFO,s); }
	@Override public void logDebug(String s) { log(Level.DEBUG,s); }
	@Override public void logTrace(String s) { log(Level.TRACE,s); }

}
