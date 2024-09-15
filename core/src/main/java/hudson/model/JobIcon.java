package hudson.model;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import org.jenkins.ui.icon.IconSpec;

/**
 * Renders {@link StatusIcon} for a Job.
 *
 * <p>
 * Possible subtypes can range from dumb icons that always render the same thing to smarter icons
 * that change its icon based on the state of the job.
 *
 * @since TODO
 */
public abstract class JobIcon extends AbstractStatusIcon implements Describable<JobIcon>, ExtensionPoint,
        IconSpec {

    private Job<?, ?> owner;

    /**
     * Called by {@link Job} to set the owner that this icon is used for.
     * @param job the job.
     */
    protected void setOwner(@Nullable Job<?, ?> job) {
        owner = job;
    }

    /**
     * Get the owner.
     * @return the job.
     */
    protected @Nullable Job<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getIconClassName() {
        return null;
    }

    @Override
    public JobIconDescriptor getDescriptor() {
        return (JobIconDescriptor) Jenkins.get().getDescriptorOrDie(getClass());
    }
}
