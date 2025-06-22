package ie.dacelonid.git;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;

import static ie.dacelonid.git.TestUtils.createBlob;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ResultOfMethodCallIgnored")
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
        assertEquals("ref: refs/heads/main\n", Files.readString(new File(tempDir, ".git/HEAD").toPath()));
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        new File(tempDir, ".git").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initFailureToCreateRefs(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        new File(tempDir, ".git/refs").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFilePrettyPrintGetExpectedContents(@TempDir File tempDir) throws Exception {
        GitCommand objUnderTest = new GitCommand();
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handle(new String[]{"cat-file", "-p", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(actualContent, outputStreamCaptor.toString().trim());
    }

}
