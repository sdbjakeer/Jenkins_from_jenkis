/*
 * The MIT License
 *
 * Copyright (c) 2019 CloudBees, Inc.
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

package jenkins.telemetry.impl.java11;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Store an event regarding missing classes. We can already catch ClassNotFoundException and NoClassDefFoundError
 */
class MissingClassEvent implements Serializable {
    private String time;
    private long occurrences;
    private String stackTrace;
    private String className;

    String getStackTrace() {
        return stackTrace;
    }

    void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    String getClassName() {
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    MissingClassEvent(@Nonnull Throwable t) {
        this.className = t.getMessage();

        StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        this.stackTrace = stackTrace.toString();

        this.time = MissingClassTelemetry.clientDateString();
        this.occurrences = 1;
    }

    String getTime() {
        return time;
    }

    long getOccurrences() {
        return occurrences;
    }

    void setOccurrences(long occurrences) {
        this.occurrences = occurrences;
    }

    void setTime(String time) {
        this.time = time;
    }
}
