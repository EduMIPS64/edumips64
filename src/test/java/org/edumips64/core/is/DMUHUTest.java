package org.edumips64.core.is;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseParsingTest;
import org.junit.Test;

public class DMUHUTest extends BaseParsingTest {
    @Test
    public void testCanParse() throws Exception {
        ParseCode("dmuhu r1, r2, r3");
    }

    @Test
    public void testName() throws Exception {
        ParseCode("dmuhu r1, r2, r3");
        var dmulu = memory.getInstruction(0);
        assertEquals("DMUHU", dmulu.getName());
    }

    @Test
    public void testRepr() throws Exception {
        ParseCode("dmuhu r1, r2, r3");
        var dmulu = memory.getInstruction(0);       
        // Check the DMULU representation, given the uncommon packing logic.
        var repr = dmulu.getRepr().getBinString();
        // SPECIAL = 000000
        assertEquals("000000", repr.substring(0, 6));
        // RS = 2 = 00010
        assertEquals("00010", repr.substring(6, 11));
        // RT = 3
        assertEquals("00011", repr.substring(11, 16));
        // RD = 1
        assertEquals("00001", repr.substring(16, 21));
        // Opcode = 00011
        assertEquals("00011", repr.substring(21, 26));
        // Special Opcode 35
        assertEquals("011101", repr.substring(26, 32));
    }
}
