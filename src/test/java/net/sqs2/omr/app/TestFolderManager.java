package net.sqs2.omr.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * A class that serves copies
 * of the resource folders
 * to the tests.
 * <p>See {@link #getFolder(String)}.
 * <p>Usage:
 * <pre>
 * public final File sourceDirectoryRoot0 = TestFolderManager.getFolder( "test0");
 * </pre>
 * <p>Please be careful with static / instance fields,
 * use instance fields when not sure.
 *
 */
public class TestFolderManager
{
    private final File rootTempDir = getTempDirCopy(new File("src/test/resources"));
    
    /**
     * static instance.
     */
    private static final TestFolderManager instance = new TestFolderManager();

    
    /**
     * Retrieves a thread safe instance.
     */
    public static TestFolderManager getInstance() {
        return instance;
    }
    
    private TestFolderManager() {
        // no other instances
    }
    private static File getTempDirCopy(File file)
    {
        File tempDir;
        try
        {
            // Use always the same name,
            // then make sure is clean
            final File tempFile = File.createTempFile("SQSREADER", "X");
            tempFile.delete();
            tempDir = new File(tempFile.getParentFile(),
            "MarkReaderAppTest_temp_DeleteSafely");
            tempDir = new File(tempDir,
            "tests");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not create temp file ??");
        }
        if (tempDir.exists()) {
            try
            {
                FileUtils.deleteDirectory(tempDir);
            }
            catch (IOException e)
            {
                // ignore
                //e.printStackTrace();
            }
            // Make sure is gone
            /*
            if (tempDir.isDirectory())
                throw new IllegalStateException("Could not delete temp dir " + tempDir);
            System.out.println("Deleted previous results: removed " + tempDir);
            */
        }
        // Recreate
        tempDir.mkdir();
        System.out.println("Created results dir: " + tempDir);
        final File actual = tempDir;
        actual.mkdir();
        try
        {
            FileUtils.copyDirectory(file, actual);
            System.out.println("Copied " + file + " to " + actual);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy " + file + " to " + actual, e);
        }
        
        return actual;
    }

    private File getTestTemp(String testName)
    {
        final File actual = new File(rootTempDir, testName);
        return actual;
    }

    
    
    /**
     * Retrieves a copy of a resource folder
     * in a temp dir. 
     * @param testName the name of the test eg: "test1"
     * @return A {@link File} that is a folder, never <code>null</code>.
     */
    public static File getFolder(String testName)
    {
        final File source = getInstance().getTestTemp(testName);

        final File forTempName;
        try
        {
            forTempName = File.createTempFile("SQSMR", "X");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not create temp file ??", e);
        }
        forTempName.delete();
        final File dest = new File(source.getParentFile(), testName + "_" + forTempName.getName());
        try
        {
            FileUtils.copyDirectory(source, dest);
            System.out.println("Made temp clone for " + testName + " in " + dest);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not copy dir", e);
        }
        return dest;
    }


}