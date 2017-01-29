package org.kisst.alecqs;

import org.kisst.alecqs.command.Command;
import org.kisst.alecqs.linesource.LineSource;
import org.kisst.alecqs.logger.SourceLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;


public class Parser {
	public final Parser parent;
	//public final LineSource src;
	public final SourceLogger logger;

	private final Map<String,String> vars = new HashMap<String, String>();
	private final File dir;
	private final Command commands;

	private File currentOutputFile = null;
	private PrintStream out = null;

	private Parser(Builder builder) {
		this.parent=builder.parent;
		//this.src=builder.src;
		this.logger=builder.logger;
		this.commands=builder.commands;
		this.dir=builder.dir;
		if (parent==null) {
			setProp("$", "$");
			setProp("@", "@");
		}
	}

	//public String readLine() { return src.getLine(); }
	public Parser getParent() { return parent; }
	public File getDir() { return dir; }


	public void setProp(String key, String value) {
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


	public void parse(LineSource src) {
		boolean done=false;
		try {
			String line;
			while ((line = src.getLine()) != null)
				parseLine(src, line);
			done=true;
		}
		finally {
			if (! done)
				System.err.println("ERROR at "+src.getLocation());
			src.close();
		}
	}


	public void parseLine(LineSource src, String line) {
		if (commands.handle(this, src, line))
			return;
		if (logger.traceEnabled())
			logger.logTrace("Parsing:"+line);
		if (line.indexOf('=')>0 && out==null) {
			line=substitute(line);
			getRoot().parseProp(line.trim());
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
		int previousPosition=0;
		int pos=str.indexOf("${");
		while (pos>=0) {
			int pos2=str.indexOf("}", pos);
			if (pos2<0)
				throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
			String key=str.substring(pos+2,pos2);
			result.append(str.substring(previousPosition,pos));

			int elvisPos= key.indexOf("?:");
			String defaultValue=null;
			if (elvisPos>0) {
				defaultValue=key.substring(elvisPos+2);
				key=key.substring(0,elvisPos);
			}
			String value= getProp(key);
			if (value==null) {
				if (key.equals("dollar"))
					value = "$";
				else if (elvisPos > 0)
					value = defaultValue;
				else
					throw new RuntimeException("Unknown variable ${" + key + "}");
			}
			result.append(value);
			previousPosition=pos2+1;
			pos=str.indexOf("${",previousPosition);
		}
		result.append(str.substring(previousPosition));
		return result.toString();
	}

	public void parseProp(String arg) {
		int pos= arg.indexOf("=");
		if (pos>0)
			setProp(arg.substring(0, pos).trim(), arg.substring(pos+1).trim());
		else
			logger.logWarn("ignoring local property definition "+ arg);
	}

	public static class Builder {
		private SourceLogger logger;
		//private LineSource src;
		private Command commands;
		private Parser parent;
		private File dir;
		public Builder() {}
		public Builder(Parser parent) {
			this.parent=parent;
			logger=parent.logger;
			commands=parent.commands;
			//src=parent.src;
			dir=parent.dir;
		}
		public Parser build() { return new Parser(this);}
		public Builder logger(SourceLogger logger) { this.logger=logger; return this; }
		//public Builder src(LineSource src) { this.src=src; return this; }
		public Builder commands(Command commands) { this.commands=commands; return this; }
		public Builder dir(File dir) { this.dir=dir; return this; }
	}
}
 
