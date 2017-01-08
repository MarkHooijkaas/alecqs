package org.kisst.alecqs;

import org.kisst.alecqs.command.BasicCommands;
import org.kisst.alecqs.command.CommandList;
import org.kisst.alecqs.logger.Logger;
import org.kisst.alecqs.logger.ParserLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;


public class Parser {
	public final Parser parent;
	public final LineSource src;
	public final Logger logger;

	private final Map<String,String> vars = new HashMap<String, String>();
	private final File dir;
	private final CommandList commands;

	private File currentOutputFile = null;
	private PrintStream out = null;

	public Parser(Parser parent, FileSource src) { this(parent, src, src.getDir()); }
	public Parser(Parser parent, LineSource src, File dir) {
		this.commands= BasicCommands.all;
		this.parent=parent;
		this.dir=dir;
		this.src=src;
		this.logger=new ParserLogger(this);
		if (parent==null) {
			setLocalProp("$", "$");
			setLocalProp("@", "@");
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
		if (logger.debugEnabled())
			logger.logDebug("setting local property "+key+"="+value);
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
		if (commands.handle(this,line))
			return;
		if (logger.traceEnabled())
			logger.logTrace("Parsing:"+line);
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
			logger.logInfo("Setting output to "+f.getName());
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
			logger.logWarn("ignoring global property definition "+ arg);
	}
	public void parseLocalProp(String arg) {
		int pos= arg.indexOf("=");
		if (pos>0)
			setLocalProp(arg.substring(0, pos).trim(), arg.substring(pos+1).trim());
		else
			logger.logWarn("ignoring local property definition "+ arg);
	}
}
 
