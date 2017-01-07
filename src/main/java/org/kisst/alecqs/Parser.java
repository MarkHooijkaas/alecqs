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

	private String currentFilename=null;
	private PrintStream out = null;
	public int currentLogLevel = WARN;

	public Parser(Parser parent, FileSource src) { this(parent, src, src.getDir()); }
	public Parser(Parser parent, LineSource src, File dir) {
		this.parent=parent;
		this.dir=dir;
		this.src=src;
		if (parent==null) {
			setGlobalProp("$", "$");
			setGlobalProp("@", "@");
			indent="";
		}
		else {
			this.currentLogLevel =parent.currentLogLevel;
			this.indent=parent.indent+"\t";
		}
		if (src instanceof FileSource) {
			FileSource f= (FileSource) src;
			setLocalProp("FILENAME", f.getFilename());
			setLocalProp("FILEBASE", f.getFilebase());
		}
	}

	public void setLocalProp(String key, String value) {
		if (activeLogLevel(DEBUG))
			log(DEBUG, "setting local property "+key+"="+value);
		vars.put(key.trim(), value);
	}
	public void setGlobalProp(String key, String value) {
		//if (activeLogLevel(DEBUG))
		//	log(DEBUG, "setting global property "+key+"="+value);
		if (parent==null)
			setLocalProp(key,value); // TODO: logs this as local property on the root Parser
		else
			parent.setGlobalProp(key, value);
	}
	public String getProp(String key) {
		String result =  vars.get(key);
		if (result != null)
			return result;
		if (parent!=null)
			return parent.getProp(key);
		return null;
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
		log(DEBUG, "Parsing:"+line);
		if (line.startsWith("@VAR"))
			parseLocalProp(substitute(line.substring(4)));
		else if (line.startsWith("@GLOBAL"))
			parseGlobalProp(substitute(line.substring(7)));
		else if (line.startsWith("@PARENT")) {
			if (parent==null)
				throw new RuntimeException("no parent in this parser context");
			parent.parseLine(line.substring(7).trim());
		}
		else if (line.startsWith("@LOGLEVEL"))
			currentLogLevel =Integer.parseInt(line.substring(9).trim());
		else if (line.startsWith("@MACRO")) {
			String name=line.substring(6).trim();
			StringBuilder macro=new StringBuilder();
			int linenr=0;
			while ((line = src.getLine()) != null) {
				if (line.trim().startsWith("@ENDMACRO"))
					break;
				else
					macro.append(line).append("\n");
			}
			setGlobalProp(name, macro.toString());
		}
		else if (line.startsWith("@RUN")) {
			line=line.substring(4);
			int pos1=line.indexOf('(');
			int pos2=line.indexOf(')');
			if (pos1<=0 || pos2<=0)
				throw new  RuntimeException("@RUN line should contain ( and )");
			String cmd=line.substring(0,pos1).trim();
			log(DEBUG, "Running macro: "+cmd);
			String macro=getProp(cmd);
			if (macro==null)
				throw new RuntimeException("could not find macro "+cmd);
			StringSource lines= new StringSource("MACRO "+cmd, macro);
			Parser p=new Parser(this, lines, dir);
			String[] args=line.substring(pos1+1,pos2).split("[,]+");
			for (String arg: args) {
				arg=arg.trim();
				if (arg.length()>0)
					p.parseLocalProp(substitute(arg));
			}
			p.parse();
		}
		else if (line.startsWith("@OUTPUTFILE"))
			changeOutputFile(substitute(line.substring(11).trim()));
		else if (line.startsWith("@LOAD")) {
			FileSource f = new FileSource(new File(dir, line.substring(5).trim()));
			Parser p= new Parser(this,f);
			p.parse();
		}
		else if (line.indexOf('=')>0 && out==null) {
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
		else
			log(WARN, "Ignoring:"+line);
	}

	private void changeOutputFile(String filename)  {
		//log(INFO, "Setting output to "+filename);

		int pos=filename.lastIndexOf('/');
		if (pos>0)
			filename=filename.substring(pos+1);
		int pos0=filename.lastIndexOf('.');
		if (pos>0)
			filename.substring(0,pos0);

		if (!filename.equals(currentFilename)) {
			if (out!=null)
				out.close();
			try {
				out=new PrintStream(new FileOutputStream(new File(dir, filename)));
			}
			catch (FileNotFoundException e) { throw new RuntimeException(e); }
			currentFilename=filename;
			log(INFO, "Setting output to "+dir.toString().replace('\\','/')+"/"+currentFilename);
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
			result.append(value.toString());
			prevpos=pos2+1;
			pos=str.indexOf("${",prevpos);
		}
		result.append(str.substring(prevpos));
		return result.toString();
	}

	public void parseGlobalProp(String arg) {
		int pos= arg.indexOf("=");
		if (pos>0)
			setGlobalProp(arg.substring(0, pos).trim(), arg.substring(pos+1).trim());
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
	private void log(int level, String s) {
		if (activeLogLevel(level))
			System.out.println(indent+src.getLocation()+":"+s);
	}


}
 
