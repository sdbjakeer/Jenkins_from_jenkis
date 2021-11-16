package jenkins.views;

import hudson.Extension;

/**
 * Default {@link Header} provided by Jenkins
 * 
 * @see Header
 */
public class JenkinsHeader extends Header {
    
    @Override
    public boolean isEnabled() {
        return true;
    }

}
