package org.kisst.alecqs.linesource;

/**
 * A source of lines
 */
public interface LineSource {
	/**
	 * Reads a line from the source
	 * @return the line, or null if no more lines are available
	 */
	public String getLine();

	/**
	 * @return a String identifying the current location, e.g. filename and linenumber
	 */
	public String getLocation();

	public void close(); // do not (yet) use AutoClosable to remain compatible with java 1.6

	public String getName();
	public int getLinenr();
}
