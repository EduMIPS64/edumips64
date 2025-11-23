package org.edumips64.core.is;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseParsingTest;
import org.junit.Test;

public class DDIVTest extends BaseParsingTest {
    // Tests for 3-parameter form (MIPS64 R6)
    @Test
    public void testCanParse3Param() throws Exception {
        parseCode("ddiv r1, r2, r3");
        // Will also include SYSCALL 0
        assertEquals(2, memory.getInstructionsNumber());
    }

    @Test
    public void testName3Param() throws Exception {
        parseCode("ddiv r1, r2, r3");
        var ddiv = memory.getInstruction(0);
        assertEquals("DDIV", ddiv.getName());
    }

    @Test
    public void testRepr3Param() throws Exception {
        parseCode("ddiv r1, r2, r3");
        var ddiv = memory.getInstruction(0);       
        // Check the DDIV representation for 3-param form.
        var repr = ddiv.getRepr().getBinString();
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
        // function = 011110 (30)
        assertEquals("011110", repr.substring(26, 32));
    }

    @Test
    public void testDivisionByZero3Param() throws Exception {
        // Test that division by zero is parsed correctly for 3-param form
        // The actual exception behavior is tested in end-to-end tests
        parseCode("daddi r1, r0, 10\nddiv r3, r1, r0");
        assertEquals(3, memory.getInstructionsNumber()); // 2 instructions + SYSCALL
    }

    // Tests for 2-parameter form (legacy)
    @Test
    public void testCanParse2Param() throws Exception {
        parseCode("ddiv r1, r2");
        // Will also include SYSCALL 0
        assertEquals(2, memory.getInstructionsNumber());
    }

    @Test
    public void testName2Param() throws Exception {
        parseCode("ddiv r1, r2");
        var ddiv = memory.getInstruction(0);
        assertEquals("DDIV", ddiv.getName());
    }

    @Test
    public void testRepr2Param() throws Exception {
        parseCode("ddiv r1, r2");
        var ddiv = memory.getInstruction(0);       
        // Check the DDIV representation for 2-param form.
        var repr = ddiv.getRepr().getBinString();
        // OPCODE = 011110
        assertEquals("011110", repr.substring(26, 32));
        // RS = 1 = 00001
        assertEquals("00001", repr.substring(6, 11));
        // RT = 2 = 00010
        assertEquals("00010", repr.substring(11, 16));
    }

    @Test
    public void testDivisionByZero2Param() throws Exception {
        // Test that division by zero is parsed correctly for 2-param form
        // The actual exception behavior is tested in end-to-end tests
        parseCode("daddi r1, r0, 10\nddiv r1, r0");
        assertEquals(3, memory.getInstructionsNumber()); // 2 instructions + SYSCALL
    }
}
