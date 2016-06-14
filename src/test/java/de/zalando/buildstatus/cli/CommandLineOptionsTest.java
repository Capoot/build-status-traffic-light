package de.zalando.buildstatus.cli;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandLineOptionsTest {

    @Test
    public void useSystemOutToDisplayStatusOptionShouldBeTrueIfSet() {
        assertTrue(new CommandLineOptions(new String[]{"--sysout"}).isSystemOutDisplay());
    }

    @Test
    public void useSystemOutToDisplayStatusOptionShouldBeFalseIfNotSet() {
        assertFalse(new CommandLineOptions(new String[]{}).isSystemOutDisplay());
    }
}
