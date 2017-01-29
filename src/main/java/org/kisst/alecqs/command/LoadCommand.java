package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.FileSource;
import org.kisst.alecqs.linesource.LineSource;

import java.io.File;

public class LoadCommand extends NamedCommand {
	public LoadCommand(String name) { super(name); }

	@Override protected void execute(Parser parser, LineSource src, String remainder) {
		File f = new File(parser.getDir(), remainder.trim());
		parser.logger.logDebug("Loading: " + f);
		FileSource fs = new FileSource(f);
		Parser p = new Parser.Builder(parser)
				.logger(parser.logger.createChildLogger(fs))
				.dir(fs.getDir())
				.build();
		p.setProp("FILENAME", fs.getFilename());
		p.setProp("FILEBASE", fs.getFilebase());
		p.parse(fs);
	}
}
