package org.edumips64.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConverterTest {

    private final String GREATER_THAN_64 = "00000000000000000000000000000000000000000000000000000000000010101";
    private final String _64_11 =  "0000000000000000000000000000000000000000000000000000000000001011";
    private final String IRREGULAR_CHARS = "blah001";
    private final String IRREGULAR_CHARS_64 = "000000000000000000000000000000000000000a000000000000000000001011";
    private final String SIGNED_64 = "1000000000000000000000000000000000000000000000000000000000000101";
    private final String BIT_LENGTH_0 = "";
    private final String OVERFLOW_64 = "1000000000000000000000000000000000000000000000000000000000000000";
    private final long EXPECTED_VALUE_FOR_SIGNED_64 = -0x7FFFFFFFFFFFFFFBL;



    @Test
    public void test64_LongUnsignedReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(_64_11, true);
        assertEquals(11, actual);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_StringLengthGreaterThan64Unsigned() throws Exception{
        Converter.binToLong(GREATER_THAN_64, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_LengthGreaterThan64Signed() throws Exception{
        Converter.binToLong(GREATER_THAN_64, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_IrregularCharsLength7Unsigned() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS, true);
    }
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_IrregularCharsLength7Signed() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS, false);
    }
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_IrregularLength64CharsUnsigned() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS_64, true);
    }
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_IrregularCharsLength64Signed() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS_64, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedStringStartsWith1() throws Exception{
        Converter.binToLong(SIGNED_64, true);
    }

    @Test
    public void test64_BitLengthZeroReturnsZero() throws Exception{
        long actual = Converter.binToLong("", true);
        assertEquals(0, actual);
        actual = Converter.binToLong("", false);
        assertEquals(0, actual);
    }

    @Test
    public void test64_SignedLen64StartsWithZeroReturnsCorrectValue()throws Exception{
        long actual = Converter.binToLong(_64_11, false);
        assertEquals(11, actual);
    }

    @Test
    public void test64_SignedStartsWithOneReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(OVERFLOW_64, false);
        long expected = (long)(-Math.pow(2.0, 63.0));
        assertEquals(expected, actual);
        actual = Converter.binToLong(SIGNED_64, false);
        assertEquals(EXPECTED_VALUE_FOR_SIGNED_64, actual);
    }



}

