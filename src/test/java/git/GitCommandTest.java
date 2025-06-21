package git;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitCommandTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(System.out);
    }

    @Test
    public void initCreatesRepo(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());
        assertTrue(new File(tempDir, ".git").exists());
        assertTrue(new File(tempDir, ".git/objects").exists());
        assertTrue(new File(tempDir, ".git/refs").exists());
        assertTrue(new File(tempDir, ".git/HEAD").exists());
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        new File(tempDir, ".git").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test //This is for when the contents are actually in a file
    public void catFilePrettyPrintGetExpectedContents(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        File file = new File(tempDir, ".git/objects/d5");
        file.mkdirs();
        File testFile = new File(file, "1f91af8d4760bc86841d6d00ce6eaf15254f38");
        testFile.createNewFile();
        String actualContent = "doo doo scooby horsey vanilla doo";
        String contentToWrite = "blob "+actualContent.length() + "\0" + actualContent;
        Files.write(testFile.toPath(), ZlibHandler.compress(contentToWrite.getBytes()));

        objUnderTest.handle(new String[]{"cat-file", "-p", "d51f91af8d4760bc86841d6d00ce6eaf15254f38"}, tempDir.toPath());

        assertEquals(actualContent, outputStreamCaptor.toString().trim());
    }
}
