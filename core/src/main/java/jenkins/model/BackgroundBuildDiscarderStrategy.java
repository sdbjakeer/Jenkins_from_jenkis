/*
 * The MIT License
 *
 * Copyright 2019 Daniel Beck
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
package jenkins.model;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Job;
import hudson.model.Run;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension point for global background build discarders.
 *
 * @see BackgroundBuildDiscarder
 * @see GlobalBuildDiscarderConfiguration
 * @see DefaultBackgroundBuildDiscarderStrategy
 */
public abstract class BackgroundBuildDiscarderStrategy extends AbstractDescribableImpl<BackgroundBuildDiscarderStrategy> implements ExtensionPoint {
    private static final Logger LOGGER = Logger.getLogger(BackgroundBuildDiscarderStrategy.class.getName());

    /**
     * Returns true if and only if this strategy applies to the given job.
     * @param job
     * @return true if and only if this strategy applies to the given job.
     */
    public abstract boolean isApplicable(Job<?, ?> job);

    /**
     * Applies this build discarder strategy to the given job, i.e. delete builds based on this strategy's configuration.
     *
     * The default implementation calls {@link #apply(Run)} on each build.
     *
     * @param job
     * @throws IOException
     * @throws InterruptedException
     */
    public void apply(Job<? extends Job, ? extends Run> job) throws IOException, InterruptedException {
        job.getBuilds().forEach(run -> {
            try {
                apply(run);
            } catch (IOException|InterruptedException ex) {
                // TODO should these actually be caught, or just thrown up to stop applying?
                LOGGER.log(Level.WARNING, "Failed to delete " + run.getFullDisplayName(), ex);
            }
        });
    }

    /**
     * Applies this build discarder strategy to the given run, i.e. delete builds based on this strategy's configuration.
     *
     * @param run
     * @throws IOException
     * @throws InterruptedException
     */
    public void apply(Run<?, ?> run) throws IOException, InterruptedException {
        // no-op by default
    }
}
