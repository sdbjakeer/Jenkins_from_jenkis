package jenkins.model;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundSetter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class JenkinsSystemReadAndManagePermissionTest {

    private static final String SYSTEM_READER_AND_MANAGER = "systemReaderAndManager";

    @BeforeClass
    public static void enablePermissions() {
        System.setProperty("jenkins.security.SystemReadPermission", "true");
        System.setProperty("jenkins.security.ManagePermission", "true");
    }

    @AfterClass
    public static void disablePermissions() {
        System.clearProperty("jenkins.security.SystemReadPermission");
        System.clearProperty("jenkins.security.ManagePermission");
    }

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private JenkinsRule.WebClient webClient;

    @Before
    public void setup() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.MANAGE, Jenkins.SYSTEM_READ, Jenkins.READ).everywhere().to(SYSTEM_READER_AND_MANAGER));

        webClient = j.createWebClient();
        webClient.setThrowExceptionOnFailingStatusCode(false);
    }

    @Test
    public void configureReadAllowedWithSystemReadAndManagePermission() throws Exception {
        HtmlPage configure = webClient.login(SYSTEM_READER_AND_MANAGER)
                .goTo("configure");
        assertThat(configure.getWebResponse().getStatusCode(), is(200));
    }

    @Test
    public void configureConfigSubmitAllowedWithSystemReadAndManagePermission() throws Exception {
        HtmlPage configure = webClient.login(SYSTEM_READER_AND_MANAGER)
                .goTo("configure");
        assertThat(configure.getWebResponse().getStatusCode(), is(200));

        HtmlForm configureForm = configure.getFormByName("config");
        HtmlPage submit = j.submit(configureForm);

        assertThat(submit.getWebResponse().getStatusCode(), is(200));
    }

    @Test
    public void cannotModifyReadOnlyConfiguration() throws Exception {
        HtmlPage configure = webClient.login(SYSTEM_READER_AND_MANAGER)
                .goTo("configure");

        //GIVEN the Global Configuration Form, with some changes unsaved
        HtmlForm form = configure.getFormByName("config");

        // WHEN a user with Jenkins.MANAGE and Jenkins.SYSTEM_READ permission tries to save the changes
        j.submit(form);
        // THEN the changes on fields forbidden to a Jenkins.MANAGE permission are not saved
        Config config = GlobalConfiguration.all().get(Config.class);

        assert config != null;
        assertNull("shouldn't be allowed to change the number of executors", config.getNumber());
    }

    @TestExtension
    public static class Config extends GlobalConfiguration {

        private String number;

        public Config() {
        }

        public String getNumber() {
            return number;
        }

        @DataBoundSetter
        public void setNumber(String number) {
            this.number = number;
            save();
        }
    }
}
