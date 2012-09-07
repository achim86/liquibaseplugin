package liquibase.eclipse.plugin.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @RunWith(SWTBotJunit4ClassRunner.class) will take a screenshot if
 * the tests fails. The screenshot is placed in a folder named 
 * screenshot within the project structure.
 * 
 * @author afinke
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class OpenViewTest {

	private static SWTWorkbenchBot	bot;
	private static SWTBotShell shell; 
	 
	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		shell = bot.activeShell();
		bot.viewByTitle("Welcome").close();
	}
 
 
	@Test
	public void test() throws Exception {
		shell.activate();
		bot.menu("Window").menu("Show View").menu("Other...").click();
		SWTBotTree tree = bot.tree(0);
		SWTBotTreeItem tItem = tree.getTreeItem("Database").expand();
		tItem.getNode("Liquibase").select();
		bot.button("OK").click();
		bot.sleep(500);
		Assert.assertEquals("Liquibase", bot.activeView().getTitle());
	}
 
 
	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}
}
