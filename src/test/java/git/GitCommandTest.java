package git;

import ie.dacelonid.git.AppConfig;
import ie.dacelonid.git.GitCommand;
import ie.dacelonid.git.ZlibHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SpringJUnitConfig(AppConfig.class)
    public class GitCommandTest {
    @Autowired
    GitCommand objUnderTest;

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
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());
        assertTrue(new File(tempDir, ".git").exists());
        assertTrue(new File(tempDir, ".git/objects").exists());
        assertTrue(new File(tempDir, ".git/refs").exists());
        assertTrue(new File(tempDir, ".git/HEAD").exists());
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo(@TempDir File tempDir) throws Exception {
        new File(tempDir, ".git").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }
    @Test
    public void initFailureToCreateRefs(@TempDir File tempDir) throws Exception {
        new File(tempDir, ".git/refs").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test //This is for when the contents are actually in a file
    public void catFilePrettyPrintGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handle(new String[]{"cat-file", "-p", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(actualContent, outputStreamCaptor.toString().trim());
    }

    private static void createBlob(File tempDir, String sha1, String actualContent) throws IOException {
        File blob = createFileForBlob(tempDir, sha1);
        String contentToWrite = "blob " + actualContent.length() + "\0" + actualContent;
        Files.write(blob.toPath(), ZlibHandler.compress(contentToWrite.getBytes()));
    }

    private static File createFileForBlob(File tempDir, String sha1) throws IOException {
        File file = new File(tempDir, ".git/objects/" + sha1.substring(0, 2));
        file.mkdirs();
        File testFile = new File(file, sha1.substring(2));
        testFile.createNewFile();
        return testFile;
    }
}
