package org.bladerunnerjs.spec.command;

import static org.bladerunnerjs.appserver.BRJSApplicationServer.Messages.*;
import static org.bladerunnerjs.plugin.plugins.commands.standard.ServeCommand.Messages.*;

import org.bladerunnerjs.appserver.ApplicationServer;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.plugin.plugins.commands.standard.ServeCommand;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.bladerunnerjs.utility.UserCommandRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// TODO: some of the integration tests for the serve command currently have to live within 'old-bladerunner-tests' given that we still rely on old bundler code in production -- move it back in once this is no longer the case
public class IntegrationServeCommandTest extends SpecTest
{
	private ApplicationServer appServer;
	private App app;
	private Aspect aspect;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).hasCommandPlugins(new ServeCommand())
			.and(brjs).hasBeenCreated()
			.and(brjs).containsFolder("apps")
			.and(brjs).containsFolder("sdk/system-applications");
		appServer = brjs.applicationServer(appServerPort);
		brjs.bladerunnerConf().setJettyPort(appServerPort);
		app = brjs.app("app1");
		aspect = app.aspect("default");
	}
	
	@After
	public void tearDown() throws Exception
	{
		logging.disableStoringLogs();
		logging.emptyLogStore();
		appServer = brjs.applicationServer(appServerPort);
		appServer.stop();
	}
	
	@Test
	public void serveCommandStartsAppServer() throws Exception
	{
		given(logging).enabled();
		when(brjs).runThreadedCommand("serve");
		then(logging).infoMessageReceived(SERVER_STARTING_LOG_MSG, "BladeRunnerJS")
			.and(logging).infoMessageReceived(SERVER_STARTED_LOG_MESSAGE, appServerPort)
			.and(logging).containsFormattedConsoleMessage(SERVER_STARTUP_MESSAGE + appServerPort +"/")
			.and(logging).containsFormattedConsoleMessage(SERVER_STOP_INSTRUCTION_MESSAGE + "\n")
			.and(appServer).requestIsRedirected("/","/dashboard");
	}
	
	@Test
	public void commandIsAutomaticallyLoaded() throws Exception
	{
		given(brjs).hasBeenAuthenticallyCreated();
			/* and */ brjs.bladerunnerConf().setJettyPort(appServerPort);
		when(brjs).runThreadedCommand("serve");
		then(exceptions).verifyNoOutstandingExceptions();
	}
	
	@Test
	public void canOverridePortValueWithArgument() throws Exception
	{
		appServerPort = 7777;
		appServer = brjs.applicationServer(appServerPort);
		
		given(logging).enabled();
		when(brjs).runThreadedCommand("serve", "-p", "7777");
		then(logging).infoMessageReceived(SERVER_STARTING_LOG_MSG, "BladeRunnerJS")
			.and(logging).infoMessageReceived(SERVER_STARTED_LOG_MESSAGE, "7777")
			.and(logging).containsFormattedConsoleMessage(SERVER_STARTUP_MESSAGE + "7777/")
			.and(logging).containsFormattedConsoleMessage(SERVER_STOP_INSTRUCTION_MESSAGE + "\n")
			.and(appServer).requestIsRedirected("/","/dashboard");
	}
	
	@Test
	public void warningIsPrintedIfTheServletJarIsOutdated() throws Exception
	{
		given(brjs).hasBeenAuthenticallyCreated()
			.and(logging).enabled()
			.and(brjs.appJars()).containsFileWithContents("brjs-servlet-1.2.3.jar", "some jar contents")
			.and(brjs.app("app1")).hasBeenPopulated()
			.and(aspect).indexPageHasContent("")
			.and(app).containsFileWithContents("WEB-INF/lib/brjs-servlet-1.2.2.jar", "old jar contents");
		when(brjs).runThreadedCommand("serve");
		then(logging).warnMessageReceived(UserCommandRunner.Messages.OUTDATED_JAR_MESSAGE, "app1", "brjs-", "sdk/libs/java/application");
	}
	
}
