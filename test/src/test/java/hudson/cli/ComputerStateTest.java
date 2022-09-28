/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.cli;

import static hudson.cli.CLICommandInvoker.Matcher.succeededSilently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.cli.CLICommandInvoker.Result;
import hudson.model.Computer;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;
import hudson.slaves.OfflineCause;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 * @author ogondza
 */
public class ComputerStateTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test
    public void connect() throws Exception {
        CLICommandInvoker command = new CLICommandInvoker(j, "connect-node");

        Slave slave = j.createSlave();
        assertTrue(slave.toComputer().isOffline());

        Result result = command.authorizedTo(Jenkins.READ, Computer.CONNECT)
                .invokeWithArgs(slave.getNodeName())
        ;

        assertThat(result, succeededSilently());
        slave.toComputer().waitUntilOnline();
        assertTrue(slave.toComputer().isOnline());
    }

    @Test
    public void online() throws Exception {
        CLICommandInvoker command = new CLICommandInvoker(j, "online-node");

        Slave slave = j.createSlave();
        assertTrue(slave.toComputer().isOffline());

        Result result = command.authorizedTo(Jenkins.READ, Computer.CONNECT)
                .invokeWithArgs(slave.getNodeName())
        ;

        assertThat(result, succeededSilently());
        slave.toComputer().waitUntilOnline();
        assertTrue(slave.toComputer().isOnline());
    }

    @Test
    public void disconnect() throws Exception {
        CLICommandInvoker command = new CLICommandInvoker(j, "disconnect-node");

        Slave slave = j.createOnlineSlave();
        assertTrue(slave.toComputer().isOnline());

        Result result = command.authorizedTo(Jenkins.READ, Computer.DISCONNECT)
                .invokeWithArgs(slave.getNodeName(), "-m", "Custom cause message")
        ;

        assertThat(result, succeededSilently());
        assertTrue(slave.toComputer().isOffline());

        OfflineCause.UserCause cause = (OfflineCause.UserCause) slave.toComputer().getOfflineCause();
        assertThat(cause.toString(), endsWith("Custom cause message"));
        assertThat(cause.getUser(), equalTo(command.user()));
    }

    @Test
    public void offline() throws Exception {
        CLICommandInvoker command = new CLICommandInvoker(j, "offline-node");

        Slave slave = j.createOnlineSlave();
        assertTrue(slave.toComputer().isOnline());

        Result result = command.authorizedTo(Jenkins.READ, Computer.DISCONNECT)
                .invokeWithArgs(slave.getNodeName(), "-m", "Custom cause message")
        ;

        assertThat(result, succeededSilently());
        assertTrue(slave.toComputer().isOffline());

        OfflineCause.UserCause cause = (OfflineCause.UserCause) slave.toComputer().getTemporarilyOfflineCause();
        assertThat(cause.toString(), endsWith("Custom cause message"));
        assertThat(cause.getUser(), equalTo(command.user()));
    }

    @Test
    public void testUiForConnected() throws Exception {
        DumbSlave slave = j.createOnlineSlave();
        Computer computer = slave.toComputer();

        WebClient wc = j.createWebClient();
        assertConnected(wc, slave);

        computer.setTemporarilyOffline(true, null);
        assertTrue(computer.isTemporarilyOffline());
        assertConnected(wc, slave);

        slave.toComputer().disconnect(null);

        HtmlPage page = wc.getPage(slave);

        assertLinkDoesNotExist(page, "Disconnect");

        assertLinkDoesNotExist(page, "Script Console");
        HtmlPage script = wc.getPage(slave, "script");
        assertThat(script.getByXPath("//form[@action='script']"), empty());

        assertLinkDoesNotExist(page, "System Information");
        HtmlPage info = wc.getPage(slave, "systemInfo");
        assertThat(info.asNormalizedText(), not(containsString("Environment Variables")));
    }

    private void assertConnected(WebClient wc, DumbSlave slave) throws Exception {
        HtmlPage main = wc.getPage(slave);
        main.getAnchorByText("Disconnect");

        main.getAnchorByText("Script Console");
        HtmlPage script = wc.getPage(slave, "script");
        assertThat(script.getByXPath("//form[@action='script']"), not(empty()));

        main.getAnchorByText("System Information");
        HtmlPage info = wc.getPage(slave, "systemInfo");
        assertThat(info.asNormalizedText(), containsString("Environment Variables"));
    }

    private void assertLinkDoesNotExist(HtmlPage page, String text) {
        assertThrows(
                text + " link should not exist",
                ElementNotFoundException.class,
                () -> page.getAnchorByText(text));
    }
}
