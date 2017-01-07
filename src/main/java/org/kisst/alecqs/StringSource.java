package org.kisst.alecqs;

public class StringSource implements LineSource {

	private final String name;
	private final String[] lines;
	private int linenr=0;

	public StringSource(String name, String lines) { this(name, lines.split("\\r\\n|\\n|\\r")); }
	public StringSource(String name, String[] lines) {
		this.name=name;
		this.lines=lines;
	}

	@Override public String getLine() {
		if (linenr>=lines.length)
			return null;
		return lines[linenr++];
	}
	@Override public String getLocation() { return name+":"+linenr;}
	@Override public void close() {}
}
