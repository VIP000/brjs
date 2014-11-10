package org.bladerunnerjs.model.engine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.memoization.MemoizedValue;
import org.bladerunnerjs.model.exception.MultipleNodesForPathException;

public class NodeList<N extends Node> {
	private final Node node;
	private final Class<N> nodeClass;
	private final Map<String, N> namedNodes = new TreeMap<>();
	private final List<NamedNodeLocator> namedNodeLocators = new ArrayList<>();
	private MemoizedValue<List<N>> list;
	
	public NodeList(Node node, Class<N> nodeClass, String subDirPath, String dirNameFilter)
	{
		this(node, nodeClass, subDirPath, dirNameFilter, null);
	}
	
	public NodeList(Node node, Class<N> nodeClass, String subDirPath, String dirNameFilter, String dirNameExcludeFilter)
	{
		this.node = node;
		this.nodeClass = nodeClass;
		namedNodeLocators.add(new DirectoryContentsNamedNodeLocator(node.root(), subDirPath, dirNameFilter, dirNameExcludeFilter));
	}
	
	public void addAlternateLocation(String subDirPath, String dirNameFilter)
	{
		addAlternateLocation(subDirPath, dirNameFilter, null);
	}
	
	public void addAlternateLocation(String subDirPath, String dirNameFilter, String dirNameExcludeFilter)
	{
		namedNodeLocators.add(new DirectoryContentsNamedNodeLocator(node.root(), subDirPath, dirNameFilter, dirNameExcludeFilter));
	}
	
	public void addAdditionalNamedLocation(String itemName, String subDirPath)
	{
		namedNodeLocators.add(new SingleDirectoryNamedNodeLocator(itemName, subDirPath));
	}
	
	@SuppressWarnings("unchecked")
	public N item(String logicalNodeName) {
		if (!namedNodes.containsKey(logicalNodeName)) {
			MemoizedFile childPath = getNodeDir(logicalNodeName);
			N child;
			try
			{
				child = (N) node.root().getRegisteredNode(childPath, nodeClass);
			}
			catch (MultipleNodesForPathException ex)
			{
				throw new RuntimeException(ex);
			}
			
			if (child == null) {
				child = (N) NodeCreator.createNode(node.root(), node, childPath, logicalNodeName, nodeClass);
			}
			
			namedNodes.put(logicalNodeName, child);
		}
		
		return namedNodes.get(logicalNodeName);
	}
	
	public List<N> list() {
		if(list == null) {
			list = new MemoizedValue<>("NodeList.list "+node.toString(), node.root(), node.dir());
		}
		
		return list.value(() -> {
			List<N> childList = new ArrayList<>();
			
			for (String nodeName : getLogicalNodeNames()) {
				childList.add(item(nodeName));
			}
			
			return childList;
		});
	}
	
	private List<String> getLogicalNodeNames()
	{
		Set<String> combinedLogicalNodeNames = new LinkedHashSet<>();
		
		for(NamedNodeLocator namedNodeLocator : namedNodeLocators)
		{
			List<String> logicalNodeNames = namedNodeLocator.getLogicalNodeNames(node.dir());
			
			for(String logicalNodeName : logicalNodeNames)
			{
				if(combinedLogicalNodeNames.contains(logicalNodeName))
				{
					throw new BladeRunnerDirectoryException("There are two directories that both have the logical name '" + logicalNodeName + "' within the directory '" + node.dir().getPath() + "'");
				}
				
				combinedLogicalNodeNames.add(logicalNodeName);
			}
			
		}
		
		return new ArrayList<>(combinedLogicalNodeNames);
	}
	
	private MemoizedFile getNodeDir(String logicalNodeName)
	{
		List<String> possibleDirNames = getPossibleDirNames(logicalNodeName);
		MemoizedFile childDir = null;
		
		for(String dirName : possibleDirNames)
		{
			MemoizedFile nextDir = node.root().getMemoizedFile(node.dir(), dirName);
			
			if(nextDir.exists())
			{
				childDir = nextDir;
				break;
			}
		}
		
		if(childDir == null) {
			childDir = node.root().getMemoizedFile(node.dir(), possibleDirNames.get(0));
		}
		
		return childDir;
	}
	
	private List<String> getPossibleDirNames(String logicalNodeName)
	{
		List<String> possibleDirNames = new ArrayList<String>();
		
		for(NamedNodeLocator namedNodeLocator : namedNodeLocators)
		{
			if(namedNodeLocator.couldSupportLogicalNodeName(logicalNodeName))
			{
				possibleDirNames.add(namedNodeLocator.getDirName(logicalNodeName));
			}
		}
		
		return possibleDirNames;
	}
}
