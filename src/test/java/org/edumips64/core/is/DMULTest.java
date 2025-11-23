package org.edumips64.core.is;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseParsingTest;
import org.junit.Test;

public class DMULTest extends BaseParsingTest {
    @Test
    public void testCanParse() throws Exception {
        parseCode("dmul r1, r2, r3");
        // Will also include SYSCALL 0
        assertEquals(2, memory.getInstructionsNumber());
    }

    @Test
    public void testName() throws Exception {
        parseCode("dmul r1, r2, r3");
        var dmul = memory.getInstruction(0);
        assertEquals("DMUL", dmul.getName());
    }

    @Test
    public void testRepr() throws Exception {
        parseCode("dmul r1, r2, r3");
        var dmul = memory.getInstruction(0);       
        // Check the DMUL representation, given the encoding.
        var repr = dmul.getRepr().getBinString();
        // SPECIAL = 000000
        assertEquals("000000", repr.substring(0, 6));
        // RS = 2 = 00010
        assertEquals("00010", repr.substring(6, 11));
        // RT = 3
        assertEquals("00011", repr.substring(11, 16));
        // RD = 1
        assertEquals("00001", repr.substring(16, 21));
        // sa = 00010
        assertEquals("00010", repr.substring(21, 26));
        // function = 011100 (28)
        assertEquals("011100", repr.substring(26, 32));
    }
}
