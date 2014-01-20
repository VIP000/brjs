package org.bladerunnerjs.spec.bundling;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Bladeset;
import org.bladerunnerjs.model.TestPack;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Before;
import org.junit.Test;


public class BladesetTestPackBundlingTest extends SpecTest
{
	private App app;
	private Bladeset bladeset;
	private TestPack bladesetUTs, bladesetATs;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlers()
    		.and(brjs).automaticallyFindsMinifiers()
    		.and(brjs).hasBeenCreated();
			app = brjs.app("app1");
			bladeset = app.bladeset("bs");
			bladesetUTs = bladeset.testType("unit").testTech("TEST_TECH");
			bladesetATs = bladeset.testType("acceptance").testTech("TEST_TECH");
	}

	@Test
	public void weBundleBladesetFilesInUTs() throws Exception {
		given(bladeset).hasPackageStyle("", "namespaced-js")
			.and(bladeset).hasClasses("appns.bs.Class1", "appns.bs.Class2")
			.and(bladeset).classRefersTo("appns.bs.Class1", "appns.bs.Class2")
			.and(bladesetUTs).testRefersTo("pkg/test.js", "appns.bs.Class1");
		then(bladesetUTs).bundledFilesEquals(
				bladeset.assetLocation("src").file("appns/bs/Class1.js"),
				bladeset.assetLocation("src").file("appns/bs/Class2.js"));
	}
	
	@Test
	public void weBundleBladesetFilesInATs() throws Exception {
		given(bladeset).hasPackageStyle("", "namespaced-js")
			.and(bladeset).hasClasses("appns.bs.Class1", "appns.bs.Class2")
			.and(bladeset).classRefersTo("appns.bs.Class1", "appns.bs.Class2")
			.and(bladesetATs).testRefersTo("pkg/test.js", "appns.bs.Class1");
		then(bladesetATs).bundledFilesEquals(
				bladeset.assetLocation("src").file("appns/bs/Class1.js"),
				bladeset.assetLocation("src").file("appns/bs/Class2.js"));
	}
	
	@Test
	public void weBundleBladesetSrcTestContentsInUTs() throws Exception {
		given(bladeset).hasPackageStyle("", "namespaced-js")
			.and(bladesetUTs).containsFile("src-test/pkg/Util.js")
			.and(bladeset).hasClasses("appns.bs.Class1")
			.and(bladeset).classDependsOn("appns.bs.Class1", "pkg.Util")
			.and(bladesetUTs).testRefersTo("test.js", "appns.bs.Class1");
		then(bladesetUTs).bundledFilesEquals(
			bladeset.assetLocation("src").file("appns/bs/Class1.js"),
			bladesetUTs.testSource().file("pkg/Util.js"));
	}
	
	@Test
	public void noExceptionsAreThrownIfTheBladesetSrcFolderHasAHiddenFolder() throws Exception {
		given(bladeset).hasPackageStyle("", "namespaced-js")
			.and(bladeset).hasClasses("appns.bs.Class1", "appns.bs.Class2")
			.and(bladeset).classRefersTo("appns.bs.Class1", "appns.bs.Class2")
			.and(bladeset).hasDir("src/.svn")
			.and(bladesetATs).testRefersTo("pkg/test.js", "appns.bs.Class1");
		then(bladesetATs).bundledFilesEquals(
			bladeset.assetLocation("src").file("appns/bs/Class1.js"),
			bladeset.assetLocation("src").file("appns/bs/Class2.js"));
	}
}
