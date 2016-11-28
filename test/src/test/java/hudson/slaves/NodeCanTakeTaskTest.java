/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.Result;
import hudson.model.Slave;
import hudson.model.queue.CauseOfBlockage;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class NodeCanTakeTaskTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Issue({"JENKINS-6598", "JENKINS-38514"})
    @Test
    public void takeBlockedByProperty() throws Exception {
        // Set master executor count to zero to force all jobs to slaves
        r.jenkins.setNumExecutors(0);
        Slave slave = r.createSlave();
        FreeStyleProject project = r.createFreeStyleProject();

        // First, attempt to run our project before adding the property
        Future<FreeStyleBuild> build = project.scheduleBuild2(0);
        r.assertBuildStatus(Result.SUCCESS, build.get(20, TimeUnit.SECONDS));

        // Add the build-blocker property and try again
        slave.getNodeProperties().add(new RejectAllTasksProperty());

        build = project.scheduleBuild2(0);
        try {
            build.get(10, TimeUnit.SECONDS);
            fail("Expected timeout exception");
        } catch (TimeoutException e) {
            List<BuildableItem> buildables = r.jenkins.getQueue().getBuildableItems();
            assertNotNull(buildables);
            assertEquals(1, buildables.size());

            BuildableItem item = buildables.get(0);
            assertEquals(project, item.task);
            assertNotNull(item.getCauseOfBlockage());
            assertEquals("rejecting everything", item.getCauseOfBlockage().getShortDescription());
        }
    }

    private static class RejectAllTasksProperty extends NodeProperty<Node> {
        @Override
        public CauseOfBlockage canTake(BuildableItem item) {
            return new CauseOfBlockage() {
                @Override
                public String getShortDescription() {
                    return "rejecting everything";
                }
            };
        }
    }
}
