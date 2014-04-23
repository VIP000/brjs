package org.bladerunnerjs.yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.bladerunnerjs.model.BRJSNode;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.utility.UnicodeReader;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlReader.YamlReaderException;

public class ConfFactory {
	public static <CF extends AbstractYamlConfFile> CF createConfFile(BRJSNode node, Class<CF> confClass, File confFile) throws ConfigException {
		CF conf = null;
		// TODO: get rid of `node == null` guard once we delete non brjs-core code
		String defaultFileCharacterEncoding = ((node == null) || confFile.getName().equals("bladerunner.conf")) ? "UTF-8" : node.root().bladerunnerConf().getDefaultFileCharacterEncoding();
		
		if(confFile.exists()) {
			conf = readConf(confFile, confClass, defaultFileCharacterEncoding);
		}
		else {
			conf = newConf(confClass);
			
		}
		
		conf.setNode(node);
		conf.setConfFile(confFile);
		conf.verify();
		
		return conf;
	}
	
	private static <CF extends AbstractYamlConfFile>  CF newConf(Class<CF> confClass)
	{
		CF conf = null;
		
		try {
			conf = confClass.newInstance();
			conf.initialize();
		}
		catch(InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return conf;
	}
	
	private static <CF extends AbstractYamlConfFile> CF readConf(File confFile, Class<CF> confClass, String defaultFileCharacterEncoding) throws ConfigException
	{
		CF conf;
		
		try {
			YamlReader reader = null;
			
			try(Reader fileReader = new UnicodeReader(confFile, defaultFileCharacterEncoding)) {
				if(!fileReader.ready()) {
					return newConf(confClass);
				}
				
				reader = new YamlReader(fileReader);
				conf = reader.read(confClass);
				conf.initialize();
			}
			finally {
				if(reader != null) {
					reader.close();
				}
			}
		}
		catch(YamlReaderException e) {
			throw new ConfigException("Parse error while reading '" + confFile.getPath() + "':\n" + e.getMessage());
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		return conf;
	}
}
