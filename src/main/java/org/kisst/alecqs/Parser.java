package org.kisst.alecqs;

import java.io.*;
import java.util.*;


public class Parser {

	public static final int NONE  = 0;
	public static final int FATAL = 10;
	public static final int ERROR = 20;
	public static final int WARN  = 30;
	public static final int INFO  = 40;
	public static final int DEBUG = 50;
	public static final int TRACE = 60;

	private final Parser parent;
	private final LineSource src;
	private final Map<String,String> vars = new HashMap<String, String>();
	private final File dir;
	private final String indent;
	private final Command[] commands;

	private File currentOutputFile = null;
	private PrintStream out = null;
	public int currentLogLevel = WARN;

	public Parser(Parser parent, FileSource src) { this(parent, src, src.getDir()); }
	public Parser(Parser parent, LineSource src, File dir) {
		this.commands=BasicCommands.all;
		this.parent=parent;
		this.dir=dir;
		this.src=src;
		if (parent==null) {
			setLocalProp("$", "$");
			setLocalProp("@", "@");
			indent="";
		}
		else {
			this.currentLogLevel = parent.currentLogLevel;
			this.indent=parent.indent+"\t";
		}
		if (src instanceof FileSource) {
			FileSource f= (FileSource) src;
			setLocalProp("FILENAME", f.getFilename());
			setLocalProp("FILEBASE", f.getFilebase());
		}
	}

	public String readLine() { return src.getLine(); }
	public Parser getParent() { return parent; }
	public File getDir() { return dir; }


	public void setLocalProp(String key, String value) {
		if (activeLogLevel(DEBUG))
			log(DEBUG, "setting local property "+key+"="+value);
		vars.put(key.trim(), value);
	}
	public String getProp(String key) {
		String result =  vars.get(key);
		if (result != null)
			return result;
		if (parent!=null)
			return parent.getProp(key);
		return null;
	}
	public Parser getRoot() {
		if (parent==null)
			return this;
		return parent.getRoot();
	}


	public void parse() {
		boolean done=false;
		try {
			String line;
			while ((line = src.getLine()) != null)
				parseLine(line);
			done=true;
		}
		finally {
			if (! done)
				System.err.println("ERROR at "+src.getLocation());
			src.close();
		}
	}


	public void parseLine(String line) {
		for (Command cmd: commands) {
			if (cmd.handle(this,line))
				return;
		}
		log(DEBUG, "Parsing:"+line);
		if (line.indexOf('=')>0 && out==null) {
			line=substitute(line);
			parseGlobalProp(line.trim());
		}
		else
			outputLine(this, line);
	}


	public void outputLine(Parser context, String line) {
		if (out!=null)
			out.println(context.substitute(line));
		else if (parent!=null)
			parent.outputLine(context, line);
		//else
		//	log(WARN, "Ignoring:"+line);
	}

	public void changeOutputFile(File f)  {
		try {
			f=f.getCanonicalFile();
		}
		catch (IOException e) { throw new RuntimeException(e); }
		if (! f.equals(currentOutputFile)) {
			if (out!=null)
				out.close();
			try {
				out=new PrintStream(new FileOutputStream(f));
			}
			catch (FileNotFoundException e) { throw new RuntimeException(e); }
			currentOutputFile=f;
			log(INFO, "Setting output to "+f.getName());
		}
	}
	public void closeOutput() {
		if (out != null)
			out.close();
	}

	public String substitute(String str) {
		if (str==null) return null;
		StringBuilder result = new StringBuilder();
		int prevpos=0;
		int pos=str.indexOf("${");
		while (pos>=0) {
			int pos2=str.indexOf("}", pos);
			if (pos2<0)
				throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
			String key=str.substring(pos+2,pos2);
			result.append(str.substring(prevpos,pos));
			String value= getProp(key);
			if (value==null && key.equals("dollar"))
				value="$";
			if (value==null)
				throw new RuntimeException("Unknown variable ${"+key+"}");
			result.append(value);
			prevpos=pos2+1;
			pos=str.indexOf("${",prevpos);
		}
		result.append(str.substring(prevpos));
		return result.toString();
	}

	public void parseGlobalProp(String arg) {
		int pos= arg.indexOf("=");
		if (pos>0)
			getRoot().setLocalProp(arg.substring(0, pos).trim(), arg.substring(pos+1).trim());
		else
			log(WARN, "ignoring global property definition "+ arg);
	}
	public void parseLocalProp(String arg) {
		int pos= arg.indexOf("=");
		if (pos>0)
			setLocalProp(arg.substring(0, pos).trim(), arg.substring(pos+1).trim());
		else
			log(WARN, "ignoring local property definition "+ arg);
	}

	private boolean activeLogLevel(int level) { return level<= currentLogLevel;}
	public void log(int level, String s) {
		if (activeLogLevel(level))
			System.out.println(indent+src.getLocation()+":"+s);
	}
}
 
