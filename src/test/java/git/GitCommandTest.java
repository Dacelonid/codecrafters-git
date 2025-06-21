package git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
    public void initCreatesRepo(@TempDir File tempDir){
        GitCommand objUnderTest = new GitCommand();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());
        assertTrue(new File(tempDir, ".git").exists());
        assertTrue(new File(tempDir, ".git/objects").exists());
        assertTrue(new File(tempDir, ".git/refs").exists());
        assertTrue(new File(tempDir, ".git/HEAD").exists());
        assertEquals("Initialized git directory", outputStreamCaptor.toString().trim());
    }

    @Test
    public void initDirectoryExistsDoesnotCreateRepo(@TempDir File tempDir){
        GitCommand objUnderTest = new GitCommand();
        new File(tempDir, ".git").mkdirs();
        objUnderTest.handle(new String[]{"init"}, tempDir.toPath());

        assertEquals("Could not initialise directory, already exists", outputStreamCaptor.toString().trim());
    }


}
