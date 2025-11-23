package org.edumips64.core.is;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseParsingTest;
import org.junit.Test;

public class DMODTest extends BaseParsingTest {
    @Test
    public void testCanParse() throws Exception {
        parseCode("dmod r1, r2, r3");
        // Will also include SYSCALL 0
        assertEquals(2, memory.getInstructionsNumber());
    }

    @Test
    public void testName() throws Exception {
        parseCode("dmod r1, r2, r3");
        var dmod = memory.getInstruction(0);
        assertEquals("DMOD", dmod.getName());
    }

    @Test
    public void testRepr() throws Exception {
        parseCode("dmod r1, r2, r3");
        var dmod = memory.getInstruction(0);       
        // Check the DMOD representation, given the encoding.
        var repr = dmod.getRepr().getBinString();
        // SPECIAL = 000000
        assertEquals("000000", repr.substring(0, 6));
        // RS = 2 = 00010
        assertEquals("00010", repr.substring(6, 11));
        // RT = 3 = 00011
        assertEquals("00011", repr.substring(11, 16));
        // RD = 1 = 00001
        assertEquals("00001", repr.substring(16, 21));
        // sa = 00010
        assertEquals("00010", repr.substring(21, 26));
        // function = 011011 (27)
        assertEquals("011011", repr.substring(26, 32));
    }

    @Test
    public void testDivisionByZero() throws Exception {
        // Test that division by zero is parsed correctly
        // The actual exception behavior is tested in end-to-end tests
        parseCode("daddi r1, r0, 10\ndmod r3, r1, r0");
        assertEquals(3, memory.getInstructionsNumber()); // 2 instructions + SYSCALL
    }
}
