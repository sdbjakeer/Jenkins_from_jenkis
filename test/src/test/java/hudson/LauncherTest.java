/*
 * The MIT License
 *
 * Copyright (c) 2013 Red Hat, Inc.
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
package hudson;

import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Slave;
import hudson.model.StringParameterDefinition;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class LauncherTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Issue("JENKINS-19488")
    @Test
    public void correctlyExpandEnvVars() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("A", "aaa"),
                new StringParameterDefinition("C", "ccc"),
                new StringParameterDefinition("B", "$A$C")
        ));
        final CommandInterpreter script = Functions.isWindows()
                ? new BatchFile("echo %A% %B% %C%")
                : new Shell("echo $A $B $C")
        ;
        project.getBuildersList().add(script);

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        rule.assertLogContains("aaa aaaccc ccc", build);
    }
    
    @Issue("JENKINS-19926")
    @Test
    public void overwriteSystemEnvVars() throws Exception {
        Map<String, String> env = new HashMap<String,String>();
        env.put("jenkins_19926", "original value");
        Slave slave = rule.createSlave(new EnvVars(env));
        
        FreeStyleProject project = rule.createFreeStyleProject();
        project.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("jenkins_19926", "${jenkins_19926} and new value")));
        final CommandInterpreter script = Functions.isWindows()
                ? new BatchFile("echo %jenkins_19926%")
                : new Shell("echo ${jenkins_19926}")
        ;
        project.getBuildersList().add(script);
        project.setAssignedNode(slave.getComputer().getNode());

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        rule.assertLogContains("original value and new value", build);
    }

    @Issue("JENKINS-23027")
    @Test public void quiet() throws Exception {
        Slave s = rule.createSlave();
        boolean windows = Functions.isWindows();
        FreeStyleProject p = rule.createFreeStyleProject();
        p.getBuildersList().add(windows ? new BatchFile("echo printed text") : new Shell("echo printed text"));
        for (Node n : new Node[] {rule.jenkins, s}) {
            rule.assertLogContains(windows ? "cmd /c" : "sh -xe", runOn(p, n));
        }
        p.getBuildersList().clear(); // TODO .replace does not seem to work
        p.getBuildersList().add(windows ? new QuietBatchFile("echo printed text") : new QuietShell("echo printed text"));
        for (Node n : new Node[] {rule.jenkins, s}) {
            rule.assertLogNotContains(windows ? "cmd /c" : "sh -xe", runOn(p, n));
        }
    }
    private FreeStyleBuild runOn(FreeStyleProject p, Node n) throws Exception {
        p.setAssignedNode(n);
        FreeStyleBuild b = rule.buildAndAssertSuccess(p);
        rule.assertLogContains("printed text", b);
        return b;
    }
    private static final class QuietLauncher extends Launcher.DecoratedLauncher {
        QuietLauncher(Launcher inner) {
            super(inner);
        }
        @Override public Proc launch(ProcStarter starter) throws IOException {
            return super.launch(starter.quiet(true));
        }
    }
    private static final class QuietShell extends Shell {
        QuietShell(String command) {
            super(command);
        }
        @Override public boolean perform(AbstractBuild<?,?> build, Launcher launcher, TaskListener listener) throws InterruptedException {
            return super.perform(build, new QuietLauncher(launcher), listener);
        }
        @Extension public static final class DescriptorImpl extends Shell.DescriptorImpl {
            @Override public String getDisplayName() {
                return "QuietShell";
            }
        }
    }
    private static final class QuietBatchFile extends BatchFile {
        QuietBatchFile(String command) {
            super(command);
        }
        @Override public boolean perform(AbstractBuild<?,?> build, Launcher launcher, TaskListener listener) throws InterruptedException {
            return super.perform(build, new QuietLauncher(launcher), listener);
        }
        @Extension public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
            @Override public String getDisplayName() {
                return "QuietBatchFile";
            }
            @SuppressWarnings("rawtypes")
            @Override public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                return true;
            }
        }
    }

    @Issue("JENKINS-52729")
    @Test public void remotable() throws Exception {
        File log = new File(rule.jenkins.root, "log");
        TaskListener listener = new RemotableBuildListener(log);
        assertEquals(0, rule.createOnlineSlave().createLauncher(listener).launch().cmds("echo", "hello").stdout(listener).join());
        assertThat(FileUtils.readFileToString(log, StandardCharsets.UTF_8).replace("\r\n", "\n"),
            containsString("[master → slave0] $ echo hello\n" +
                           "[master → slave0] hello"));
    }
    private static class RemotableBuildListener implements BuildListener {
        private static final long serialVersionUID = 1;
        /** location of log file streamed to by multiple sources */
        private final File logFile;
        /** records allocation & deserialization history; e.g., {@code master → agentName} */
        private final String id;
        private transient PrintStream logger;
        RemotableBuildListener(File logFile) {
            this(logFile, "master");
        }
        private RemotableBuildListener(File logFile, String id) {
            this.logFile = logFile;
            this.id = id;
        }
        @Override public PrintStream getLogger() {
            if (logger == null) {
                final OutputStream fos;
                try {
                    fos = new FileOutputStream(logFile, true);
                    logger = new PrintStream(new LineTransformationOutputStream() {
                        @Override protected void eol(byte[] b, int len) throws IOException {
                            fos.write(("[" + id + "] ").getBytes(StandardCharsets.UTF_8));
                            fos.write(b, 0, len);
                        }
                    }, true, "UTF-8");
                } catch (IOException x) {
                    throw new AssertionError(x);
                }
            }
            return logger;
        }
        private Object writeReplace() {
            Thread.dumpStack();
            String name = Channel.current().getName();
            return new RemotableBuildListener(logFile, id + " → " + name);
        }
    }

}
