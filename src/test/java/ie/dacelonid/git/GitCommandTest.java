package ie.dacelonid.git;

import ie.dacelonid.git.utils.TreeEntry;
import org.junit.jupiter.api.AfterEach;
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

import static ie.dacelonid.git.TestUtils.*;
import static ie.dacelonid.git.utils.GitTreeParser.serializeTree;
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
    public void hashObjectCreatesFileAndPrintsSHA1(@TempDir File tempDir) throws Exception {
        String actualContent = "mango apple blueberry orange pear raspberry";
        String expectedSha1 = "64d73c5f262a3a02dc16ca2c86b0828c34e179f4";
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath()); //Need to initialize repo so we can write the blob
        //create file with the content of ActualContent
        writeToFile(new File(tempDir, "filename.txt"), actualContent);
        objUnderTest.handleCommand(new String[]{"hash-object", "-w", "filename.txt"}, tempDir.toPath());

        String[] output = outputStreamCaptor.toString().trim().split("\\R");
        String actualSha1 = output[output.length - 1];

        assertEquals(expectedSha1, actualSha1);

        //Check file exists
        assertTrue(tempDir.toPath().resolve(".git/objects/" + actualSha1.substring(0, 2) + "/" + actualSha1.substring(2)).toFile().exists());

        //Check file contents
        assertEquals(actualContent, readBlob(tempDir, "64d73c5f262a3a02dc16ca2c86b0828c34e179f4"));
    }

    @Test
    public void lsTreeExistingTreePrintsOutTheNamesOfTheDirectories(@TempDir File tempDir) throws Exception {
        objUnderTest.handleCommand(new String[]{"init"}, tempDir.toPath()); //Need to initialize repo so we can create the dirs

        String[] expectedResult = {"dir1", "dir2", "filename.txt"};
        String givenSha1 = createTreeFile(tempDir, expectedResult);

        objUnderTest.handleCommand(new String[]{"ls-tree", "--name-only ", givenSha1}, tempDir.toPath());

        String[] actualOutput = Arrays.stream(outputStreamCaptor.toString().split("\\R"))
                .skip(1) //Skip the first entry in the outputStream as it is the Initialize Empty Repository print out
                .toArray(String[]::new);
        assertArrayEquals(expectedResult, actualOutput);
    }

    private String createTreeFile(File tempDir, String[] fileContents) throws IOException {
        String givenSha1 = "be80d6b281f79ac42fe6e632209841a2dcadb061"; //hardcoded valid sha1 hash, but isn't a hash of the actual contents of the file
        File treeFile = createFileForBlob(tempDir, givenSha1);
        Files.write(treeFile.toPath(), ZlibHandler.compress(getTreeFileContents(fileContents)));
        return givenSha1;
    }


    public byte[] getTreeFileContents(String[] fileContents) {
        List<TreeEntry> entries = new ArrayList<>();
        String mode = "";
        for(String fileContent : fileContents) {
            if(fileContent.startsWith("dir"))
                mode = "040000";
            else
                mode = "100644";
            entries.add(new TreeEntry(mode, fileContent, new byte[20]));
        }
        return serializeTree(entries);
    }
}
