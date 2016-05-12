/*
 *
 */
package hudson;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.Issue;

public class RemoveWindowsDirectoryJunctionTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void windowsOnly() {
       assumeTrue(File.pathSeparatorChar==';');
    }

    @Test
    @Issue("JENKINS-2995")
    public void testJunctionIsRemovedButNotContents() throws Exception {
        TemporaryFolder dir1 = new TemporaryFolder(tmp.getRoot());
        dir1.create();
        File dir1Root = dir1.getRoot();
        File f1 = dir1.newFile();
        File j1 = makeJunction(tmp.getRoot(), dir1Root);
        print(tmp.getRoot());
        print(dir1Root);
        print(j1);
        Util.deleteRecursive(j1);
        print(dir1Root);
        print(tmp.getRoot());
        assertTrue(f1.exists());
    }

    private File makeJunction(File baseDir, File pointToDir) throws Exception {
       File junc = new File(baseDir, "testJunction");
       Process p = Runtime.getRuntime().exec("cmd.exe /C \"mklink /J " + junc.getPath() + " " + pointToDir.getPath() + "\"");
       p.waitFor();
       return junc;
    }
    
    private void print(File d) {
        System.out.println(d.getPath());
        String[] c = d.list();
        for (String s : c) {
            System.out.println("   '" + s + "'");
        }
    }
}