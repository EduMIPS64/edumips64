package org.edumips64.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConverterTest {

    //32 bit
    private final String OVER_32_MSB_0 =   "000000000000000000000000000010101";
    private final String OVER_32_MSB_1 =   "100000000000000000000000000010101";
    private final String EQUALS_32_MSB_1 = "10000000000000000000000000000101";
    private final String EQUALS_32_MSB_0 = "00000000000000000000000000000101";
    private final String UNDER_32_MSB_1 =  "1000000000000000000000000000101";
    private final String UNDER_32_MSB_0 =  "0000000000000000000000000000101";
    private final int EXPECTED_FOR_SIGNED_EQUALS_32 = -0x7FFFFFFB;
    private final int EXPECTED_FOR_SIGNED_UNDER_32 =  -0x3FFFFFFB;

    //64 bit
    private final String OVER_64_MSB_0 =   "00000000000000000000000000000000000000000000000000000000000010101";
    private final String OVER_64_MSB_1 =   "10000000000000000000000000000000000000000000000000000000000010101";
    private final String EQUALS_64_MSB_0 = "0000000000000000000000000000000000000000000000000000000000000101";
    private final String EQUALS_64_MSB_1 = "1000000000000000000000000000000000000000000000000000000000000101";
    private final String UNDER_64_MSB_0 =  "000000000000000000000000000000000000000000000000000000000000101";
    private final String UNDER_64_MSB_1 =  "100000000000000000000000000000000000000000000000000000000000101";
    private final long EXPECTED_FOR_SIGNED_EQUALS_64 = -0x7FFFFFFFFFFFFFFBL;
    private final long EXPECTED_FOR_SIGNED_UNDER_64 =  -0x3FFFFFFFFFFFFFFBL;

    //Irregular, overflow, zero length
    private final String NONBINARY_EQUALS_32_MSB_0 = "0000000a000000000000000000001011";
    private final String NONBINARY_EQUALS_32_MSB_1 = "1000000a000000000000000000001011";
    private final String NONBINARY_UNDER_32_MSB_0 =  "000000a000000000000000000001011";
    private final String NONBINARY_UNDER_32_MSB_1 =  "100000a000000000000000000001011";
    private final String OVERFLOW_32 = "10000000000000000000000000000000";
    private final int OVERFLOW_INT_EXPECTED = -0x80000000;

    private final String NONBINARY_EQUALS_64_MSB_0 = "000000000000000000000000a000000000000000000000000000000000001011";
    private final String NONBINARY_EQUALS_64_MSB_1 = "100000000000000000000000a000000000000000000000000000000000001011";
    private final String NONBINARY_UNDER_64_MSB_0 =  "00000000000000000000000a000000000000000000000000000000000001011";
    private final String NONBINARY_UNDER_64_MSB_1 =  "10000000000000000000000a000000000000000000000000000000000001011";
    private final String OVERFLOW_64 = "1000000000000000000000000000000000000000000000000000000000000000";
    private final long OVERFLOW_LONG_EXPECTED = -0x8000000000000000L;

    private final String BIT_LENGTH_0 = "";

    //32 Greater than
    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedLenOver32Msb0() throws Exception{
        Converter.binToInt(OVER_32_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedLenOver32Msb1() throws Exception{
        Converter.binToInt(OVER_32_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedLenOver32Msb0() throws Exception{
        Converter.binToInt(OVER_32_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedLenOver32Msb1() throws Exception{
        Converter.binToInt(OVER_32_MSB_1, true);
    }

    //32 Equals

    @Test
    public void test32_SignedLenEquals32Msb0() throws Exception{
        int actual = Converter.binToInt(EQUALS_32_MSB_0, false);
        int expected = Integer.parseInt(EQUALS_32_MSB_0, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void test32_SignedLenEquals32Msb1() throws Exception{
        int actual = Converter.binToInt(EQUALS_32_MSB_1, false);
        assertEquals(EXPECTED_FOR_SIGNED_EQUALS_32, actual);
    }

    @Test
    public void test32_UnsignedLenEquals32Msb0() throws Exception{
        int actual = Converter.binToInt(EQUALS_32_MSB_0, true);
        assertEquals(5, actual);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedLenEquals32Msb1() throws Exception{
        Converter.binToInt(EQUALS_32_MSB_1, true);

    }

    //32 Less than
    @Test
    public void test32_SignedLenUnder32Msb0() throws Exception{
        int actual = Converter.binToInt(UNDER_32_MSB_0, false);
        int expected = Integer.parseInt(UNDER_32_MSB_0, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void test32_SignedLenUnder32Msb1() throws Exception{
        int actual = Converter.binToInt(UNDER_32_MSB_1, false);
        assertEquals(EXPECTED_FOR_SIGNED_UNDER_32, actual);
    }

    @Test
    public void test32_UnsignedLenUnder32Msb0() throws Exception{
        int actual = Converter.binToInt(UNDER_32_MSB_0, true);
        assertEquals(5, actual);
    }

    @Test
    public void test32_UnsignedLenUnder32Msb1() throws Exception{
        int actual = Converter.binToInt(UNDER_32_MSB_1, true);
        int expected = Integer.parseInt(UNDER_32_MSB_1, 2);
        assertEquals(expected, actual);
    }

    //32 Irregular, overflow

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedNonBinLen32Msb0() throws Exception{
        Converter.binToInt(NONBINARY_EQUALS_32_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedNonBinLen32Msb1() throws Exception{
        Converter.binToInt(NONBINARY_EQUALS_32_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedNonBinLen32Msb0() throws Exception{
        Converter.binToInt(NONBINARY_EQUALS_32_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedNonBinLen32Msb1() throws Exception{
        Converter.binToInt(NONBINARY_EQUALS_32_MSB_1, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedNonBinLenUnder32Msb0() throws Exception{
        Converter.binToInt(NONBINARY_UNDER_32_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_SignedNonBinLenUnder32Msb1() throws Exception{
        Converter.binToInt(NONBINARY_UNDER_32_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedNonBinLenUnder32Msb0() throws Exception{
        Converter.binToInt(NONBINARY_UNDER_32_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test32_UnsignedNonBinLenUnder32Msb1() throws Exception{
        Converter.binToInt(NONBINARY_UNDER_32_MSB_1, true);
    }

    @Test
    public void test32_SignedOverflowReturnsCorrectValue() throws Exception{
        int actual = Converter.binToInt(OVERFLOW_32, false);
        assertEquals(OVERFLOW_INT_EXPECTED, actual);
    }

    //64 Greater than

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedLenOver64Msb0() throws Exception{
        Converter.binToLong(OVER_64_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedLenOver64Msb1() throws Exception{
        Converter.binToLong(OVER_64_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedLenOver64Msb0() throws Exception{
        Converter.binToLong(OVER_64_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedLenOver64Msb1() throws Exception{
        Converter.binToLong(OVER_64_MSB_1, true);
    }

    //64 Equals

    @Test
    public void test64_SignedLenEquals64Msb0() throws Exception{
        long actual = Converter.binToLong(EQUALS_64_MSB_0, false);
        long expected = Long.parseLong(EQUALS_64_MSB_0, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void test64_SignedLenEquals64Msb1() throws Exception{
        long actual = Converter.binToLong(EQUALS_64_MSB_1, false);
        assertEquals(EXPECTED_FOR_SIGNED_EQUALS_64, actual);
    }

    @Test
    public void test64_UnsignedLenEquals64Msb0() throws Exception{
        long actual = Converter.binToLong(EQUALS_64_MSB_0, true);
        assertEquals(5, actual);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedLenEquals64Msb1() throws Exception{
        Converter.binToLong(EQUALS_64_MSB_1, true);

    }

    //64 Less than
    @Test
    public void test64_SignedLenUnder64Msb0() throws Exception{
        long actual = Converter.binToLong(UNDER_64_MSB_0, false);
        long expected = Long.parseLong(UNDER_64_MSB_0, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void test64_SignedLenUnder64Msb1() throws Exception{
        long actual = Converter.binToLong(UNDER_64_MSB_1, false);
        assertEquals(EXPECTED_FOR_SIGNED_UNDER_64, actual);
    }

    @Test
    public void test64_UnsignedLenUnder64Msb0() throws Exception{
        long actual = Converter.binToLong(UNDER_64_MSB_0, true);
        assertEquals(5, actual);
    }

    @Test
    public void test64_UnsignedLenUnder64Msb1() throws Exception{
        long actual = Converter.binToLong(UNDER_64_MSB_1, true);
        long expected = Long.parseLong(UNDER_64_MSB_1, 2);
        assertEquals(expected, actual);
    }

    //64 Irregular, overflow

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedNonBinLen64Msb0() throws Exception{
        Converter.binToLong(NONBINARY_EQUALS_64_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedNonBinLen64Msb1() throws Exception{
        Converter.binToLong(NONBINARY_EQUALS_64_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedNonBinLen64Msb0() throws Exception{
        Converter.binToLong(NONBINARY_EQUALS_64_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedNonBinLen64Msb1() throws Exception{
        Converter.binToLong(NONBINARY_EQUALS_64_MSB_1, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedNonBinLenUnder64Msb0() throws Exception{
        Converter.binToLong(NONBINARY_UNDER_64_MSB_0, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_SignedNonBinLenUnder64Msb1() throws Exception{
        Converter.binToLong(NONBINARY_UNDER_64_MSB_1, false);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedNonBinLenUnder64Msb0() throws Exception{
        Converter.binToLong(NONBINARY_UNDER_64_MSB_0, true);
    }

    @Test(expected = IrregularStringOfBitsException.class)
    public void test64_UnsignedNonBinLenUnder64Msb1() throws Exception{
        Converter.binToLong(NONBINARY_UNDER_64_MSB_1, true);
    }

    @Test
    public void test64_SignedOverflowReturnsCorrectValue() throws Exception{
        long actual = Converter.binToLong(OVERFLOW_64, false);
        assertEquals(OVERFLOW_LONG_EXPECTED, actual);
    }

    @Test
    public void test64_BitLengthZeroReturnsZero() throws Exception{
        long actual = Converter.binToLong(BIT_LENGTH_0, false);
        assertEquals(0, actual);
        actual = Converter.binToLong(BIT_LENGTH_0, true);
        assertEquals(0, actual);
    }

    /** Tests for parseImmediate */
    @Test
    public void ParseImmediateCorrectValues() throws Exception {
        assertEquals(Converter.parseImmediate("1"), 1L);
        assertEquals(Converter.parseImmediate("#1"), 1L);
        assertEquals(Converter.parseImmediate("0x1"), 1L);
        assertEquals(Converter.parseImmediate("0X1"), 1L);
        assertEquals(Converter.parseImmediate("#0x1"), 1L);

        // Hexadecimal.
        assertEquals(Converter.parseImmediate("0xA"), 10L);
        assertEquals(Converter.parseImmediate("0xA"), 10L);
        assertEquals(Converter.parseImmediate("0xDEADBEEF"), 3735928559L);

        // Negative numbers.
        assertEquals(Converter.parseImmediate("-1"), -1L);
        assertEquals(Converter.parseImmediate("#-1"), -1L);
        assertEquals(Converter.parseImmediate("0x-1"), -1L);

        // Explicit plus sign.
        assertEquals(Converter.parseImmediate("+1"), 1L);
        assertEquals(Converter.parseImmediate("#+1"), 1L);
        assertEquals(Converter.parseImmediate("0x+A"), 10L);
        assertEquals(Converter.parseImmediate("#0x+A"), 10L);
        assertEquals(Converter.parseImmediate("#0x+A1"), 161L);
    }

    @Test(expected = NumberFormatException.class)
    public void ParseImmediateEmptyString() throws Exception {
        Converter.parseImmediate("");
    }

    @Test(expected = NumberFormatException.class)
    public void ParseImmediateInvalidHex() throws Exception {
        Converter.parseImmediate("xA");
    }

    @Test(expected = NumberFormatException.class)
    public void ParseImmediateNonnumericString() throws Exception {
        Converter.parseImmediate("foo");
    }
}

