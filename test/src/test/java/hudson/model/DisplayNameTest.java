/*
 *  The MIT License
 * 
 *  Copyright 2011 Yahoo!, Inc.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package hudson.model;

import jenkins.model.Jenkins;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author kingfai
 *
 */
public class DisplayNameTest extends HudsonTestCase {

    @Test
    public void testRenameJobWithNoDisplayName() throws Exception {
        final String projectName = "projectName";
        final String newProjectName = "newProjectName";
        FreeStyleProject project = createFreeStyleProject(projectName);
        Assert.assertEquals(projectName, project.getDisplayName());
        
        project.renameTo(newProjectName);
        Assert.assertEquals(newProjectName, project.getDisplayName());
    }
    
    @Test
    public void testRenameJobWithDisplayName() throws Exception {
        final String projectName = "projectName";
        final String newProjectName = "newProjectName";
        final String displayName = "displayName";
        FreeStyleProject project = createFreeStyleProject(projectName);
        project.setDisplayName(displayName);
        Assert.assertEquals(displayName, project.getDisplayName());
        
        project.renameTo(newProjectName);
        Assert.assertEquals(displayName, project.getDisplayName());        
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testCopyJobWithNoDisplayName() throws Exception {
        final String projectName = "projectName";
        final String newProjectName = "newProjectName";
        FreeStyleProject project = createFreeStyleProject(projectName);
        Assert.assertEquals(projectName, project.getDisplayName());

        AbstractProject newProject = Jenkins.getInstance().copy((AbstractProject)project, newProjectName);
        Assert.assertEquals(newProjectName, newProject.getName());
        Assert.assertEquals(newProjectName, newProject.getDisplayName());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testCopyJobWithDisplayName() throws Exception {
        final String projectName = "projectName";
        final String newProjectName = "newProjectName";
        final String oldDisplayName = "oldDisplayName";
        FreeStyleProject project = createFreeStyleProject(projectName);
        project.setDisplayName(oldDisplayName);
        Assert.assertEquals(oldDisplayName, project.getDisplayName());

        AbstractProject newProject = Jenkins.getInstance().copy((AbstractProject)project, newProjectName);
        Assert.assertEquals(newProjectName, newProject.getName());
        Assert.assertEquals(newProjectName, newProject.getDisplayName());
        
    }
}
