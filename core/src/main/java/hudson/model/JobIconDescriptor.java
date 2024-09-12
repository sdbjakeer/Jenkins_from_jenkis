package hudson.model;

import hudson.DescriptorExtensionList;
import jenkins.model.Jenkins;

/**
 * Job icon descriptor.
 *
 * @since TODO
 */
public abstract class JobIconDescriptor extends Descriptor<JobIcon> {

    public static DescriptorExtensionList<JobIcon, JobIconDescriptor> all() {
        return Jenkins.get().getDescriptorList(JobIcon.class);
    }

    /**
     * Returns true if this {@link Job} type is applicable to the
     * given job type.
     * @param jobType the type of job.
     * @return true to indicate applicable, in which case the icon will be
     * displayed in the configuration screen of this job.
     */
    public boolean isApplicable(Class<? extends Job<?, ?>> jobType) {
        return true;
    }
}
