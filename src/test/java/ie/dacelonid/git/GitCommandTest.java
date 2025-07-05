package ie.dacelonid.git;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;

import static ie.dacelonid.git.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class GitCommandTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private GitCommand objUnderTest;

    @TempDir
    static File tempDir;

    @BeforeAll
    public static void setupOnce() throws IOException {
        copyGitObjectsFromResources(tempDir.toPath());
    }

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
    public void initCreatesRepo(@TempDir File tempDir) throws Exception { //needs blank dir to initialize repo
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());
        assertTrue(new File(tempDir, ".git").exists());
        assertTrue(new File(tempDir, ".git/objects").exists());
        assertTrue(new File(tempDir, ".git/refs").exists());
        assertTrue(new File(tempDir, ".git/HEAD").exists());
        assertEquals("ref: refs/heads/main\n", Files.readString(new File(tempDir, ".git/HEAD").toPath()));
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo() throws Exception {
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());
        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initFailureToCreateRefs() throws Exception {
        new File(tempDir, ".git/refs").mkdirs();
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFilePrettyPrintGetExpectedContents() throws Exception {
        String sha1 = "282b3544f154ec2ef0192a3bfad9614f7d8ab665";
        String actualContent = "apple banana cherry flavour";
        objUnderTest.handleCommand(new String[]{"cat-file", "-p", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(actualContent, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileShowTypeGetExpectedContents() throws Exception {
        String sha1 = "282b3544f154ec2ef0192a3bfad9614f7d8ab665";
        String expectedType = "blob";

        objUnderTest.handleCommand(new String[]{"cat-file", "-t", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(expectedType, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileShowSizeGetExpectedContents() throws Exception {
        String sha1 = "282b3544f154ec2ef0192a3bfad9614f7d8ab665";
        String expectedSize = "28";

        objUnderTest.handleCommand(new String[]{"cat-file", "-s", sha1}, tempDir.toPath());

        assertTrue(tempDir.toPath().resolve(".git/objects/" + sha1.substring(0, 2) + "/" + sha1.substring(2)).toFile().exists());
        assertEquals(expectedSize, outputStreamCaptor.toString().trim());
    }

    @Test
    public void catFileCheckObjectExistsGetExpectedContents() throws Exception {
        String sha1 = "282b3544f154ec2ef0192a3bfad9614f7d8ab665";
        objUnderTest.handleCommand(new String[]{"cat-file", "-e", sha1}, tempDir.toPath());
        assertEquals(0, outputStreamCaptor.toString().trim().length());
    }

    @Test
    public void catFileCheckObjectExistsWhenItDoesnotGetExpectedContents() throws Exception {
        String sha1 = "d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        String expectedOutput = "fatal: Not a valid object name d51f91af8d4760bc86841d6d00ce6eaf15254f38";
        objUnderTest.handleCommand(new String[]{"cat-file", "-e", sha1}, tempDir.toPath());

        assertEquals(expectedOutput, outputStreamCaptor.toString().trim());
    }


    @Test
    public void hashObjectCreatesFileAndPrintsSHA1() throws Exception {
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
    public void lsTreeExistingTreePrintsOutTheNamesOnly() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};
        String givenSha1 = createTreeFile(tempDir, expectedResult);

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }

    @Test//needs updating
    public void lsTreeExistingTreePrintsOnlyDirectories() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};
        String givenSha1 = createTreeFile(tempDir, expectedResult);

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }

    @Test //needs changing
    public void lsTreeExistingTreePrintsOnlyObjects() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};
        String givenSha1 = createTreeFile(tempDir, expectedResult);

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }


    @Test//needs changing
    public void lsTreeExistingRecurseIntoTrees() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};
        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }


}
