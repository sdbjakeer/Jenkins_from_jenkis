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

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.DescribableList;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Collections;

/**
 * Global configuration UI for background build discarders
 *
 * @see BackgroundBuildDiscarderStrategy
 * @see BackgroundBuildDiscarder
 */
@Restricted(NoExternalUse.class)
@Extension @Symbol("globalBuildDiscarders")
public class GlobalBuildDiscarderConfiguration extends GlobalConfiguration {
    public static GlobalBuildDiscarderConfiguration get() {
        return ExtensionList.lookupSingleton(GlobalBuildDiscarderConfiguration.class);
    }

    private final DescribableList<BackgroundBuildDiscarderStrategy, BackgroundBuildDiscarderStrategyDescriptor> configuredBuildDiscarders =
            new DescribableList<>(this, Collections.singletonList(new DefaultBackgroundBuildDiscarderStrategy()));

    private Object readResolve() {
        configuredBuildDiscarders.setOwner(this);
        return this;
    }

    public DescribableList<BackgroundBuildDiscarderStrategy, BackgroundBuildDiscarderStrategyDescriptor> getConfiguredBuildDiscarders() {
        return configuredBuildDiscarders;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            configuredBuildDiscarders.rebuildHetero(req, json, BackgroundBuildDiscarderStrategyDescriptor.all(), "configuredBuildDiscarders");
            return true;
        } catch (IOException x) {
            throw new FormException(x, "artifactManagerFactories");
        }
    }
}
