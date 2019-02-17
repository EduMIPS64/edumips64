package org.edumips64.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConverterTest {

    //32 bit
    private final String GREATER_THAN_32 = "000000000000000000000000000010101";
    private final String _32_11 =  "00000000000000000000000000001011";
    private final String IRREGULAR_CHARS_32 = "0000000a000000000000000000001011";
    private final String SIGNED_32 = "10000000000000000000000000000101";
    private final String OVERFLOW_32 = "10000000000000000000000000000000";
    private final long EXPECTED_VALUE_FOR_SIGNED_32 = -0x7FFFFFFBL;

    //64 bit
    private final String GREATER_THAN_64 = "00000000000000000000000000000000000000000000000000000000000010101";
    private final String _64_11 =  "0000000000000000000000000000000000000000000000000000000000001011";
    private final String IRREGULAR_CHARS_64 = "000000000000000000000000a000000000000000000000000000000000001011";
    private final String SIGNED_64 = "1000000000000000000000000000000000000000000000000000000000000101";
    private final String OVERFLOW_64 = "1000000000000000000000000000000000000000000000000000000000000000";
    private final long EXPECTED_VALUE_FOR_SIGNED_64 = -0x7FFFFFFFFFFFFFFBL;

    //common
    private final String IRREGULAR_CHARS = "blah001";
    private final String BIT_LENGTH_0 = "";


    //******Begin 32 bit tests

    /**
     * Tests that a 32 length bit string value returns the correct long value
     */
    @Test
    public void test32_UnsignedIntReturnsCorrectValue() throws Exception{
        long actual = Converter.binToInt(_32_11, true);
        assertEquals(11, actual);
    }

    /**
     * Tests that an unsigned string length greater than 32 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedLenOver32() throws Exception{
        Converter.binToInt(GREATER_THAN_32, true);
    }

    /**
     * Tests that a signed string length greater than 32 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedLenOver32() throws Exception{
        Converter.binToInt(GREATER_THAN_32, false);
    }

    /**
     * Tests that an unsigned string length of 7 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedIrregularCharLen7() throws Exception{
        Converter.binToInt(IRREGULAR_CHARS, true);
    }

    /**
     * Tests that a signed string length of 7 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedIrregularCharLen7() throws Exception{
        Converter.binToInt(IRREGULAR_CHARS, false);
    }

    /**
     * Tests that an unsigned string length of 32 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedIrregularCharLen32() throws Exception{
        Converter.binToInt(IRREGULAR_CHARS_32, true);
    }

    /**
     * Tests that a signed string length of 32 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedIrregularCharsLen32() throws Exception{
        Converter.binToInt(IRREGULAR_CHARS_32, false);
    }

    /**
     * Tests that an unsigned string length 32 whose msb is 1 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedMsbOne() throws Exception{
        Converter.binToInt(SIGNED_32, true);
    }

    /**
     * Tests that signed length 32 string of bits with msb 0 returns the correct long value
     */
    @Test
    public void test32_SignedLen32MsbZeroReturnsCorrectValue()throws Exception{
        long actual = Converter.binToInt(_32_11, false);
        assertEquals(11, actual);
    }

    /**
     * Tests that a signed length 32 string of bits with msb 1 returns the correct long value
     */
    @Test
    public void test32_SignedLen32MsbOneReturnsCorrectValue() throws Exception{
        long actual = Converter.binToInt(SIGNED_32, false);
        assertEquals(EXPECTED_VALUE_FOR_SIGNED_32, actual);
    }

    /**
     *Tests that signed length 32 with msb 1 and all others bits 0 returns correct int value
     */
    @Test
    public void test32_SignedOverflowReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(OVERFLOW_32, false);
        long expected = (long)(-Math.pow(2.0, 31.0));
        assertEquals(expected, actual);
    }


    //******Begin 64 bit tests


    /**
     * Tests that a 64 length bit string value returns the correct long value
     */
    @Test
    public void test64_UnsignedLongReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(_64_11, true);
        assertEquals(11, actual);
    }

    /**
     * Tests that an unsigned string length greater than 64 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedLenOver64() throws Exception{
        Converter.binToLong(GREATER_THAN_64, true);
    }

    /**
     * Tests that a signed string length greater than 64 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedLenOver64() throws Exception{
        Converter.binToLong(GREATER_THAN_64, false);
    }

    /**
     * Tests that an unsigned string length of 7 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedIrregularCharLen7() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS, true);
    }

    /**
     * Tests that a signed string length of 7 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedIrregularCharLen7() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS, false);
    }

    /**
     * Tests that an unsigned string length of 64 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedIrregularCharLen64() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS_64, true);
    }

    /**
     * Tests that a signed string length of 64 containing characters other than
     * 0 or 1 throws an IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedIrregularCharsLen64() throws Exception{
        Converter.binToLong(IRREGULAR_CHARS_64, false);
    }

    /**
     * Tests that an unsigned string length 64 whose msb is 1 throws IrregularStringOfBitsException
     */
    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedMsbOne() throws Exception{
        Converter.binToLong(SIGNED_64, true);
    }

    /**
     * Tests that signed and unsigned empty string returns value of 0
     */
    @Test
    public void test64_BitLengthZeroReturnsZero() throws Exception{
        long actual = Converter.binToLong(BIT_LENGTH_0, true);
        assertEquals(0, actual);
        actual = Converter.binToLong(BIT_LENGTH_0, false);
        assertEquals(0, actual);
    }

    /**
     * Tests that signed length 64 string of bits with msb 0 returns the correct long value
     */
    @Test
    public void test64_SignedLen64MsbZeroReturnsCorrectValue()throws Exception{
        long actual = Converter.binToLong(_64_11, false);
        assertEquals(11, actual);
    }

    /**
     * Tests that a signed length 64 string of bits with msb 1 returns the correct long value
     */
    @Test
    public void test64_SignedLen64MsbOneReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(SIGNED_64, false);
        assertEquals(EXPECTED_VALUE_FOR_SIGNED_64, actual);
    }

    /**
     *Tests that signed length 64 with msb 1 and all others bits 0 returns correct long value
     */
    @Test
    public void test64_SignedOverflowReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(OVERFLOW_64, false);
        long expected = (long)(-Math.pow(2.0, 63.0));
        assertEquals(expected, actual);
    }



}

