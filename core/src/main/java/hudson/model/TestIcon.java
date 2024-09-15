package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Renders a test icon for a Job.
 */
public class TestIcon extends JobIcon {

    @DataBoundConstructor
    public TestIcon() { /* NOP */ }

    @Override
    public String getImageOf(String size) {
        return null;
    }

    @Override
    public String getIconClassName() {
        return "symbol-edit";
    }

    @Override
    public String getDescription() {
        return "Testing";
    }

    @Extension
    public static class DescriptorImpl extends JobIconDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return "Test Icon";
        }
    }
}
