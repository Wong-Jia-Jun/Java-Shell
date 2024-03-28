package sg.edu.nus.comp.cs4218.impl;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Environment test.
 */
class EnvironmentTest {

    /**
     * Current directory variable value should be initialized to jvm user dir.
     */
    @Test
    void currentDirectory_VariableValue_ShouldBeInitializedToJVMUserDir() {
        String expected = System.getProperty("user.dir");
        assertEquals(expected, Environment.currentDirectory);
    }

}
