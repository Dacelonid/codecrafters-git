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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ie.dacelonid.git.TestUtils.copyGitObjectsFromResources;
import static ie.dacelonid.git.TestUtils.readBlob;
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
    public void catFileShortenedNameExpectedContents() throws Exception {
        String sha1 = "282b3544f154ec2ef0192a3bfad9614f7d8ab665";
        String expectedType = "blob";

        objUnderTest.handleCommand(new String[]{"cat-file", "-t", "282b"}, tempDir.toPath());

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
        Files.write(new File(tempDir, "filename.txt").toPath(), actualContent.getBytes());
        objUnderTest.handleCommand(new String[]{"hash-object", "-w", "filename.txt"}, tempDir.toPath());

        String[] expectedResult = {"64d73c5f262a3a02dc16ca2c86b0828c34e179f4"};
        verifyOutput(expectedResult);

        //Check file exists
        assertTrue(tempDir.toPath().resolve(".git/objects/" + expectedSha1.substring(0, 2) + "/" + expectedSha1.substring(2)).toFile().exists());

        //Check file contents
        assertEquals(actualContent, readBlob(tempDir, "64d73c5f262a3a02dc16ca2c86b0828c34e179f4"));
    }

    @Test
    public void lsTreeExistingTreePrintsOutTheNamesOnly() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};
        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        verifyOutput(expectedResult);
    }

    @Test//needs updating
    public void lsTreeExistingTreePrintsOnlyDirectories() throws Exception {
        String[] expectedResult = {"dir1", "dir2"};

        objUnderTest.handleCommand(new String[]{"ls-tree", "-d", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        verifyOutput(expectedResult);
    }

    @Test //needs changing
    public void lsTreeExistingTreePrintsOnlyObjects() throws Exception {
        String[] expectedResult = {"dir1", "dir2", "file1.txt"};

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        verifyOutput(expectedResult);
    }


    @Test//needs changing
    public void lsTreeExistingRecurseIntoTrees() throws Exception {
        String[] expectedResult = {"dir1", "dir11", "file2.txt", "file3.txt", "dir12", "file4.txt", "file1.txt", "dir2", "file5.txt", "file6.txt", "file1.txt"};
        objUnderTest.handleCommand(new String[]{"ls-tree", "-r", "f77b5382e65983a53f2b3cf01be995b5449ba307"}, tempDir.toPath());

        verifyOutput(expectedResult);
    }

    @Test
    public void writeTreeCreatesTreesAndBlobs(@TempDir File tempDir) throws Exception {
        String[] expectedResult = {"a8593a036daaf80874a009d97dbbf1c484b43434"};
        Files.write(new File(tempDir, "test32").toPath(), "hello World 1".getBytes());
        Files.write(new File(tempDir, "test33").toPath(), "hello World 2".getBytes());
        File subdir = new File(tempDir, "subdir");
        subdir.mkdirs();
        Files.write(new File(subdir, "test34").toPath(), "hello World 4".getBytes());
        objUnderTest.handleCommand(new String[]{"write-tree"}, tempDir.toPath());

        verifyOutput(expectedResult);

        verifyAllSha1sAreCorrectAndAccountedFor(tempDir);
    }

    private void verifyAllSha1sAreCorrectAndAccountedFor(File tempDir) throws Exception {
        List<TestData> testData = getTestData();
        for(TestData td:testData){
            outputStreamCaptor.reset();
            objUnderTest.handleCommand(new String[]{"cat-file", "-p", td.sha1()}, tempDir.toPath());
            verifyOutput(td.expectedOutput());
            outputStreamCaptor.reset();
            objUnderTest.handleCommand(new String[]{"cat-file", "-t", td.sha1()}, tempDir.toPath());
            verifyOutput(new String[]{td.type()} );
        }
    }

    private static List<TestData> getTestData() {
        List<TestData>testData = new ArrayList<>();
        testData.add(new TestData("1551da829dad8697a8e55cc6c4e8033dc66f031c",new String[]{"hello World 2"}, "blob"));
        testData.add(new TestData("3b167a44261258c3c1e351089ec7de6bc43f73f9",new String[]{"hello World 1"}, "blob"));
        testData.add(new TestData("6f0684f76f518604ca40ea553612a7a00abc690b",new String[]{"hello World 4"}, "blob"));
        testData.add(new TestData("a8593a036daaf80874a009d97dbbf1c484b43434",new String[]{"040000 tree 5aa1306163b3971a731a90d3b29046f37809fdaf subdir", "100644 blob 3b167a44261258c3c1e351089ec7de6bc43f73f9 test32", "100644 blob 1551da829dad8697a8e55cc6c4e8033dc66f031c test33" }, "tree"));
        testData.add(new TestData("5aa1306163b3971a731a90d3b29046f37809fdaf",new String[]{"100644 blob 6f0684f76f518604ca40ea553612a7a00abc690b test34"}, "tree"));
        return testData;
    }

    private void verifyOutput(String[] expectedResult) {
        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R")).toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput, Arrays.toString(actualOutput));
    }

}
