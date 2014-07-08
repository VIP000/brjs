package org.bladerunnerjs.plugin.plugins.commands.standard;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.logging.Logger;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.JsLib;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.model.exception.command.NodeDoesNotExistException;
import org.bladerunnerjs.plugin.utility.command.ArgsParsingCommandPlugin;
import org.bladerunnerjs.utility.EncodedFileUtil;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

public class JsDocCommand extends ArgsParsingCommandPlugin {
	public static final String APP_STORAGE_DIR_NAME = "jsdoc";

	public class Messages {
		public static final String API_DOCS_GENERATED_MSG = "API docs successfully generated in '%s'";
	}
	
	private BRJS brjs;
	private EncodedFileUtil fileUtil;
	private Logger logger;
	
	@Override
	public void setBRJS(BRJS brjs) {	
		try {
			this.brjs = brjs;
			this.logger = brjs.logger(this.getClass());
			fileUtil = new EncodedFileUtil(brjs.bladerunnerConf().getDefaultFileCharacterEncoding());
		}
		catch(ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getCommandName() {
		return APP_STORAGE_DIR_NAME;
	}
	
	@Override
	public String getCommandDescription() {
		return "Generate JsDocs for a given application";
	}
	
	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		argsParser.registerParameter(new UnflaggedOption("app-name").setRequired(true).setHelp("the application for which jsdoc will be generated"));
		argsParser.registerParameter(new Switch("verbose-flag").setShortFlag('v').setLongFlag("verbose").setDefault("false").setHelp("display more console output while generating the jsdoc output"));
	}
	
	@Override
	protected int doCommand(JSAPResult parsedArgs) throws CommandArgumentsException, CommandOperationException {
		String appName = parsedArgs.getString("app-name");
		boolean isVerbose = parsedArgs.getBoolean("verbose-flag");
		App app = brjs.app(appName);
		
		if(!app.dirExists()) throw new NodeDoesNotExistException(app, this);
		
		File outputDir = app.storageDir(APP_STORAGE_DIR_NAME);
		CommandRunnerUtility.runCommand(brjs, generateCommand(app, isVerbose, outputDir));
		
		try {
			replaceBuildDateToken(new File(outputDir, "index.html"));
			replaceVersionToken(new File(outputDir, "index.html"));
		}
		catch(IOException | ConfigException e) {
			throw new CommandOperationException(e);
		}
		
		logger.println(Messages.API_DOCS_GENERATED_MSG, outputDir.getPath());
		
		return 0;
	}
	
	private List<String> generateCommand(App app, boolean isVerbose, File outputDir) throws CommandOperationException {
		List<String> command = new ArrayList<>();
		
		try {
			File jsdocToolkitInstallDir = installJsdocToolkit();
			File jsDocToolkitDir = new File(jsdocToolkitInstallDir, "jsdoc-toolkit");
			File jsDocTemplatesDir = new File(jsdocToolkitInstallDir, "jsdoc-template");
			List<String> libraryPaths = new ArrayList<>();
			
			for (JsLib jsLib : app.jsLibs()) {
				libraryPaths.add(jsLib.file("src").getCanonicalFile().getAbsolutePath());
			}
			
			// See <https://code.google.com/p/jsdoc-toolkit/wiki/CmdlineOptions>
			command.add("java");
			command.add("-jar");
			command.add(new File(jsDocToolkitDir, "jsrun.jar").getAbsolutePath());
			command.add(new File(jsDocToolkitDir, "app/run.js").getAbsolutePath());
			command.addAll(libraryPaths);
			command.add("-r=20");
			command.add("-t=" + jsDocTemplatesDir.getAbsolutePath());
			command.add("-d=" + outputDir.getAbsolutePath());
			command.add((isVerbose) ? "-v" : "-q");
		}
		catch (IOException ex) {
			throw new CommandOperationException(ex);
		}
		
		return command;
	}
	
	private File installJsdocToolkit() throws IOException {
		File installDir = brjs.storageDir("jsdoc-toolkit-install");
		
		if(!installDir.exists()) {
			installDir.mkdirs();
			FileUtils.copyDirectory(brjs.template("jsdoc").dir(), installDir);
		}
		
		return installDir;
	}
	
	private void replaceBuildDateToken(File indexFile) throws IOException, ConfigException {
		String fileContent = fileUtil.readFileToString(indexFile);
		
		DateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy");
		Date date = new Date();
		
		String resultFileContent = fileContent.replace("@buildDate@", dateFormat.format(date));
		fileUtil.writeStringToFile(indexFile, resultFileContent);
	}
	
	private void replaceVersionToken(File indexFile) throws IOException, ConfigException {
		String fileContent = fileUtil.readFileToString(indexFile);
		
		String resultFileContent = fileContent.replace("@sdkVersion@", brjs.versionInfo().getVersionNumber());
		fileUtil.writeStringToFile(indexFile, resultFileContent);
	}
}
