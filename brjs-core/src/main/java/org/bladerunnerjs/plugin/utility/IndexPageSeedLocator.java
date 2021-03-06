package org.bladerunnerjs.plugin.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.memoization.MemoizedValue;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.LinkedFileAsset;
import org.bladerunnerjs.model.LinkedAsset;

public class IndexPageSeedLocator {
	private final Map<String, LinkedAsset> cachedAssets = new HashMap<>();
	private final MemoizedValue<List<LinkedAsset>> seedAssetsList;
	
	public IndexPageSeedLocator(BRJS brjs) {
		seedAssetsList = new MemoizedValue<>("AssetLocation.seedAssets", brjs, brjs.dir());
	}
	
	public List<LinkedAsset> seedAssets(BundlableNode viewableBundlableNode) {
		return seedAssetsList.value(() -> {
			List<LinkedAsset> seedFiles = new ArrayList<>();
			MemoizedFile indexFile = getIndexFile(viewableBundlableNode);
			
			if (indexFile != null) {
				String indexFilePath = indexFile.getAbsolutePath();
				
				if(!cachedAssets.containsKey(indexFilePath)) {
					cachedAssets.put(indexFilePath, new LinkedFileAsset(indexFile, viewableBundlableNode.assetLocation("resources")));
				}
				
				seedFiles.add(cachedAssets.get(indexFilePath));
			}
			
			return seedFiles;
		});
	}
	
	private MemoizedFile getIndexFile(BundlableNode viewableBundlableNode) {
		MemoizedFile indexFile = null;
		
		if(viewableBundlableNode.file("index.html").exists()) {
			indexFile = viewableBundlableNode.file("index.html");
		}
		else if(viewableBundlableNode.file("index.jsp").exists()) {
			indexFile = viewableBundlableNode.file("index.jsp");
		}
		
		return indexFile;
	}
}
