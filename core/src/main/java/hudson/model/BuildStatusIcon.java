package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Renders {@link BallColor} as icon for a Job.
 *
 * @since TODO
 */
public class BuildStatusIcon extends JobIcon {

    @DataBoundConstructor
    public BuildStatusIcon() { /* NOP */ }

    @Override
    public String getImageOf(String size) {
        return getBuildStatus().getImageOf(size);
    }

    @Override
    public String getIconClassName() {
        return "symbol-status-" + getBuildStatus().getIconName();
    }

    @Override
    public String getDescription() {
        return getBuildStatus().getDescription();
    }

    protected BallColor getBuildStatus() {
        Job<?, ?> job = getOwner();
        if (job != null) {
            return job.getIconColor();
        }

        return BallColor.NOTBUILT;
    }

    @Extension
    public static class DescriptorImpl extends JobIconDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.BuildStatusIcon_DisplayName();
        }
    }
}
