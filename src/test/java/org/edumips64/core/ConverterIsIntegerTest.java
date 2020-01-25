package org.edumips64.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ConverterIsIntegerTest {
    @Parameters(name = "{index}:isNumber({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"", false}, {"+", false}, {"-", false}, {" ", false}, {"++", false},
            {"0", true}, {"+0", true}, {"-0", true}, {"00000000000", true},
            {"0xFF", false}, {"123", true}, {"foo", false,}
        });

    }

    @Parameter
    public String num;

    @Parameter(1)
    public boolean expected;

    @Test
    public void test() {
        assertEquals(expected, Converter.isInteger(num));
    }
}