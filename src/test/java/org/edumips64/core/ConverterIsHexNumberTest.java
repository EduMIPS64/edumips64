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
public class ConverterIsHexNumberTest {
    @Parameters(name = "{index}:isHexNumber({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"", false}, {"+", false}, {"-", false}, {" ", false}, {"++", false},
            {"0", false}, {"+0", false}, {"-0", false}, {"00000000000", false},
            {"0x", false}, {"0X", false},
            {"0xFF", true}, {"0XFF", true}, {"0x1", true}, {"0x0", true}
        });

    }

    @Parameter
    public String num;

    @Parameter(1)
    public boolean expected;

    @Test
    public void test() {
        assertEquals(expected, Converter.isHexNumber(num));
    }
}