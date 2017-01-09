package org.kisst.alecqs.logger;

import org.kisst.alecqs.linesource.LineSource;

public interface SourceLogger extends Logger{
	public SourceLogger createChildLogger(LineSource src);
}
