/*
 * The MIT License
 *
 * Copyright 2018 CloudBees, Inc.
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
package jenkins.model.logging.impl;

import hudson.console.AnnotatedLargeText;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import jenkins.model.logging.Loggable;
import jenkins.model.logging.LogStorage;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Default Logging Method implementation which does nothing
 * @author Oleg Nenashev
 * @since TODO
 */
@Restricted(Beta.class)
public class NoopLogStorage extends LogStorage {

    public NoopLogStorage(Loggable loggable) {
        super(loggable);
    }
    private transient File noopLogFile;

    @Nonnull
    @Override
    public BuildListener createBuildListener() throws IOException, InterruptedException {
        return new BuildListener() {
            @Nonnull
            @Override
            public PrintStream getLogger() {
                return TaskListener.NULL.getLogger();
            }
        };
    }

    @Override
    public AnnotatedLargeText overallLog() {
        return new BrokenAnnotatedLargeText(
                new UnsupportedOperationException("Browsing is not supported"),
                getOwner().getCharset());
    }

    @Override
    public InputStream getLogInputStream() throws IOException {
        throw new IOException("Browsing is not supported");
    }

    @Override
    public List<String> getLog(int maxLines) throws IOException {
        throw new IOException("Browsing is not supported");
    }

    //TODO: It may be better to have a single file for all implementations, but Charsets may be different
    @Nonnull
    @Override
    public File getLogFile() throws IOException {
        if (noopLogFile == null) {
            File f = File.createTempFile("deprecated", ".log", getOwner().getTmpDir());
            f.deleteOnExit();
            try (OutputStream os = new FileOutputStream(f)) {
                overallLog().writeRawLogTo(0, os);
            }
            noopLogFile = f;
        }
        return noopLogFile;
    }

    @Override
    public boolean deleteLog() {
        return true;
    }
}
