package org.kisst.alecqs.command;

import org.kisst.alecqs.Parser;
import org.kisst.alecqs.linesource.FileSource;

import java.io.File;

public class LoadCommand extends NamedCommand {
	public LoadCommand(String name) { super(name); }

	@Override protected void execute(Parser parser, String remainder) {
		File f = new File(parser.getDir(), remainder.trim());
		parser.logger.logDebug("Loading: " + f);
		FileSource fs = new FileSource(f);
		Parser p = new Parser.Builder(parser)
				.src(fs)
				.logger(parser.logger.createChildLogger(fs))
				.dir(fs.getDir())
				.build();
		p.setLocalProp("FILENAME", fs.getFilename());
		p.setLocalProp("FILEBASE", fs.getFilebase());
		p.parse();
	}
}
