package org.kisst.alecqs.linesource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileSource implements LineSource {

	private final BufferedReader inp;
	private final File file;
	private final String filename;
	private final String filebase;
	private int linenr=0;

	public FileSource(File file) {
		this.file=file;
		try {
			this.inp = new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	    this.filename=file.getName().trim().replace('\\','/');

		int pos0=filename.lastIndexOf('.');
      	this.filebase=filename.substring(0,pos0);
	}

	@Override public String getName() { return filename;}
	@Override public int getLinenr() { return linenr; }
	public File getDir() { return file.getParentFile();}
	public String getFilename() { return filename;}
	public String getFilebase() { return filebase;}

	@Override public String getLine() {
		linenr++;
		try {
			return inp.readLine();
		}
		catch (IOException e) {throw new RuntimeException(e); }
	}

	@Override public String getLocation() { return file.getName()+":"+linenr;}

	@Override public void close() {
		try {
			inp.close();
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}



}
