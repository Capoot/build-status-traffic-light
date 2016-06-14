package de.zalando.buildstatus.cli;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CliFlagsAndOptionsTest {

    @Test
    public void isFlagSetShouldBeTrueIfFlagIsSet() {
        assertTrue(CliFlagsAndOptions.isFlagSet('f', new String[]{"-f"}));
    }

    @Test
    public void isFlagSetShouldBeFalseIfFlagIsNotSet() {
        assertFalse(CliFlagsAndOptions.isFlagSet('f', new String[]{}));
    }

    @Test
    public void flasShouldBeCaseSensitive() {
        assertFalse(CliFlagsAndOptions.isFlagSet('f', new String[]{"-F"}));
    }

    @Test
    public void multipleFlagsShouldBeCombinable() {
        assertTrue(CliFlagsAndOptions.isFlagSet('a', new String[]{"-ab"}));
        assertTrue(CliFlagsAndOptions.isFlagSet('b', new String[]{"-ab"}));
    }

    @Test
    public void optionShouldBeRecognizedWithoutMinusSign() {
        assertEquals("value", CliFlagsAndOptions.getOption("option", new String[]{"--option", "value"}));
    }

    @Test
    public void optionShouldBeRecognizedWithMinusSign() {
        assertEquals("value", CliFlagsAndOptions.getOption("--option", new String[]{"--option", "value"}));
    }

    @Test
    public void optionShouldNotBeRecognizedWithSingleMinusSign() {
        assertNull(CliFlagsAndOptions.getOption("-option", new String[]{"--option", "value"}));
    }

    @Test
    public void optionShouldBeCaseInsensitive() {
        assertEquals("value", CliFlagsAndOptions.getOption("--Option", new String[]{"--option", "value"}));
    }

    @Test
    public void ifOptionIsNotSetResultShouldBeNull() {
        assertNull(CliFlagsAndOptions.getOption("option", new String[]{}));
    }

    @Test
    public void emptyOptionShouldYieldNull() {
        assertNull(CliFlagsAndOptions.getOption("", new String[]{}));
    }

    @Test
    public void nullOptionShouldYieldNull() {
        assertNull(CliFlagsAndOptions.getOption(null, new String[]{}));
    }
}
