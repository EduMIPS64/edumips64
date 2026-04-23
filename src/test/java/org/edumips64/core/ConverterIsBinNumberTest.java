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
public class ConverterIsBinNumberTest {
    @Parameters(name = "{index}:isBinNumber({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"", false}, {"+", false}, {"-", false}, {" ", false}, {"++", false},
            {"0", false}, {"+0", false}, {"-0", false}, {"00000000000", false},
            {"0b", false}, {"0B", false},
            {"0b2", false}, {"0bhello", false}, {"0b102", false},
            {"0xFF", false}, {"123", false},
            {"0b0", true}, {"0b1", true}, {"0B1", true}, {"0b1010", true}, {"0B1010", true}
        });
    }

    @Parameter
    public String num;

    @Parameter(1)
    public boolean expected;

    @Test
    public void test() {
        assertEquals(expected, Converter.isBinNumber(num));
    }
}
