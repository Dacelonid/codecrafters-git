package ie.dacelonid.git;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;

import static ie.dacelonid.git.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class GitCommandTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private GitCommand objUnderTest;

    @BeforeEach
    public void setUp() {
        objUnderTest = new GitCommand();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(System.out);
    }


    @Test
    public void initCreatesRepo(@TempDir File tempDir) throws Exception {
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());
        assertTrue(new File(tempDir, ".git").exists());
        assertTrue(new File(tempDir, ".git/objects").exists());
        assertTrue(new File(tempDir, ".git/refs").exists());
        assertTrue(new File(tempDir, ".git/HEAD").exists());
        assertEquals("ref: refs/heads/main\n", Files.readString(new File(tempDir, ".git/HEAD").toPath()));
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo(@TempDir File tempDir) throws Exception {
        new File(tempDir, ".git").mkdirs();
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initFailureToCreateRefs(@TempDir File tempDir) throws Exception {
        new File(tempDir, ".git/refs").mkdirs();
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFilePrettyPrintGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handleCommand(new String[]{"cat-file", "-p", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(actualContent, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileShowTypeGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";
        String expectedType = "blob";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handleCommand(new String[]{"cat-file", "-t", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(expectedType, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileShowSizeGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";
        String expectedSize = "33";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handleCommand(new String[]{"cat-file", "-s", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(expectedSize, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileCheckObjectExistsGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String actualContent = "doo doo scooby horsey vanilla doo";

        createBlob(tempDir, sha1, actualContent);

        objUnderTest.handleCommand(new String[]{"cat-file", "-e", sha1}, tempDir.toPath());

        assertEquals(0, outputStreamCaptor.toString().trim().length());
    }

    @Test
    public void catFileCheckObjectExistsWhenItDoesnotGetExpectedContents(@TempDir File tempDir) throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String expectedOutput = "fatal: Not a valid object name d51f91af8d4760bc86841d6d00ce6eaf15254f38";

        objUnderTest.handleCommand(new String[]{"cat-file", "-e", sha1}, tempDir.toPath());

        assertEquals(expectedOutput, outputStreamCaptor.toString().trim());
    }


    @Test
    public void hashObjectCreatesFileAndPrintsSHA1(@TempDir File tempDir) throws Exception {
        String actualContent = "mango apple blueberry orange pear raspberry";
        String expectedSha1 = "64d73c5f262a3a02dc16ca2c86b0828c34e179f4";
        //create file with the content of ActualContent
        writeToFile(new File(tempDir, "filename.txt"), actualContent);
        objUnderTest.handleCommand(new String[]{"hash-object", "-w", "filename.txt"}, tempDir.toPath());

        String[] output = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        String[] expectedResult = {"64d73c5f262a3a02dc16ca2c86b0828c34e179f4"};

        assertArrayEquals(expectedResult, output);

        //Check file exists
        assertTrue(tempDir.toPath().resolve(".git/objects/" + expectedSha1.substring(0, 2) + "/" + expectedSha1.substring(2)).toFile().exists());

        //Check file contents
        assertEquals(actualContent, readBlob(tempDir, "64d73c5f262a3a02dc16ca2c86b0828c34e179f4"));
    }

    @Test
    public void lsTreeExistingTreePrintsOutTheNamesOfTheDirectories(@TempDir File tempDir) throws Exception {
        String[] expectedResult = {"dir1", "dir2", "filename.txt"};
        String givenSha1 = createTreeFile(tempDir, expectedResult);

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", givenSha1}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }
}
