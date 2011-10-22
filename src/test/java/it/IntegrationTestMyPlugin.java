package it;

public class IntegrationTestMyPlugin extends AbstractIntegrationTestCase
{
	public void testSomething()
	{
        // TODO: Make a dummy space an use the atlas-create-home-zip command to create an export suitable for testing
        gotoPage("");
        assertTextPresent("Welcome");
	}
}
