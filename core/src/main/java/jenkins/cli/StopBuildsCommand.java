/*
 * The MIT License
 *
 * Copyright (c) 2018, Ilia Zasimov
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
package jenkins.cli;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.Executor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.args4j.Argument;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Extension
@Restricted(NoExternalUse.class)
public class StopBuildsCommand extends CLICommand {

    @VisibleForTesting
    @Argument(usage = "Name of the job(s) to stop", required = true, multiValued = true)
    /*private */ List<String> jobNames;

    private boolean isAnyBuildStopped;

    @Override
    public String getShortDescription() {
        return "Stop all running builds for job(s)";
    }

    @Override
    protected int run() throws Exception {
        Jenkins jenkins = Jenkins.get();
        final HashSet<String> names = new HashSet<>();
        names.addAll(jobNames);

        final List<Job> jobsToStop = new ArrayList<>();
        for (final String jobName : names) {
            Item item = jenkins.getItemByFullName(jobName);
            if (item instanceof Job) {
                jobsToStop.add((Job) item);
            } else {
                throw new IllegalArgumentException("Invalid job name = " + jobName);
            }
        }

        for (final Job job : jobsToStop) {
            stopJobBuilds(job, job.getName());
        }

        if (!isAnyBuildStopped) {
            stdout.println("No builds stopped;");
        }

        return 0;
    }

    private void stopJobBuilds(final Job job,
                               final String jobName) throws IOException, ServletException {
        final Run lastBuild = job.getLastBuild();
        if (lastBuild != null && lastBuild.isBuilding()) {
            stopBuild(lastBuild, jobName);
            checkAndStopPreviousBuilds(lastBuild, jobName);
        }
    }

    private void stopBuild(final Run build,
                           final String jobName) throws IOException, ServletException {
        final String buildName = build.getDisplayName();
        Executor executor = build.getExecutor();
        if (executor != null) {
            try {
                executor.doStop();
                isAnyBuildStopped = true;
                stdout.println(String.format("Build %s stopped for job %s;", buildName, jobName));
            } catch (final Exception e) {
                stdout.println(String.format("Exception occurred while trying to stop build %s for job %s;", buildName, jobName));
                stdout.println(String.format("Exception class: %s, message: %s;", e.getClass().getSimpleName(), e.getMessage()));
            }
        } else {
            stdout.println(String.format("Build %s in job %s not stopped;", buildName, jobName));
        }
    }

    private void checkAndStopPreviousBuilds(final Run lastBuild,
                                            final String jobName) throws IOException, ServletException {
        Run build = lastBuild.getPreviousBuildInProgress();
        while (build != null) {
            stopBuild(build, jobName);
            build = build.getPreviousBuildInProgress();
        }
    }

}
