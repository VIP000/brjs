package org.bladerunnerjs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.RootNode;

public class BladesetWorkbench extends Workbench<Bladeset> {

	public BladesetWorkbench(RootNode rootNode, Node parent, File dir)
	{
		super(rootNode, parent, dir);
	}
		
	public Bladeset parent()
	{
		return (Bladeset) parentNode();
	}
	
	@Override
	public List<AssetContainer> scopeAssetContainers() {
		List<AssetContainer> assetContainers = new ArrayList<>();
		for (JsLib jsLib : app().jsLibs())
		{
			assetContainers.add( jsLib );			
		}
		Bladeset bladeset = root().locateAncestorNodeOfClass(this, Bladeset.class);
		assetContainers.add(bladeset);
		assetContainers.addAll(bladeset.blades());
		assetContainers.add(this);		
		return assetContainers;
	}
}
