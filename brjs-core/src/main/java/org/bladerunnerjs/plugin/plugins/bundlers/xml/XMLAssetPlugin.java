package org.bladerunnerjs.plugin.plugins.bundlers.xml;

import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.plugin.base.AbstractAssetPlugin;

public class XMLAssetPlugin extends AbstractAssetPlugin {
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}
	
	@Override
	public boolean canHandleAsset(MemoizedFile assetFile, AssetLocation assetLocation) {
		String assetName = assetFile.getName();
		// TODO: should the aliases xml filtering be moved into the model once getAssets() has been deleted?
		return (assetName.endsWith(".xml") && !assetName.equals("aliases.xml")  && !assetName.equals("aliasDefinitions.xml"));
	}
	
	@Override
	public Asset createAsset(MemoizedFile assetFile, AssetLocation assetLocation) throws AssetFileInstantationException {
		return new XMLAsset(assetFile, assetLocation);
	}
}
