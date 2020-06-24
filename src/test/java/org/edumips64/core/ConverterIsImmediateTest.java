package org.edumips64.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ConverterIsImmediateTest {
    @Parameters(name = "{index}:isImmediate({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"", false}, {"+", false}, {"-", false}, {" ", false}, {"++", false},
            {"0", true}, {"+0", true}, {"-0", true}, {"00000000000", true},
            {"0xFF", true}, {"123", true}, {"foo", false},
            {"#", false}, {"#1", true}, {"#0xFFFF", true}, {"0x8000", true},
            {"#0xFFFFFFF", false}, {"0xFFFFFFF", false}
        });

    }

    @Parameter
    public String num;

    @Parameter(1)
    public boolean expected;

    @Test
    public void test() {
        assertEquals(expected, Converter.isImmediate(num));
    }
}