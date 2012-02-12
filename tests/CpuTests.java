/* CpuTests.java
 *
 * Tests for the EduMIPS64 CPU.
 *
 * (c) 2012 Andrea Spadaccini
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edumips64.tests;

import edumips64.core.*;
import edumips64.core.is.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


public class CpuTests {
    protected CPU cpu;
    protected Parser parser;

    @Before
    public void setUp() {
        cpu = CPU.getInstance();
        cpu.setStatus(CPU.CPUStatus.READY);
        parser = Parser.getInstance();
    }

    /** Runs a MIPS64 test program, raising an exception if it does not succeed.
     *
     * @param testPath path of the test code.
     */
    protected void runMipsTest(String testPath) throws Exception {
        try {
            parser.parse(testPath);
            cpu.setStatus(CPU.CPUStatus.RUNNING);
            while(true) {
                cpu.step();
            }
        } finally {
            cpu.reset();
        }
    }

    /* Test for utils/strlen.s */
    @Test(expected = HaltException.class)
    public void testStrlen() throws Exception {
        runMipsTest("tests/data/test-strlen.s");
    }

    /* Test for utils/strcmp.s */
    @Test(expected = HaltException.class)
    public void testStrcmp() throws Exception {
        runMipsTest("tests/data/test-strcmp.s");
    }

    /* ------- REGRESSION TESTS -------- */
    /* Issue #7 */
    @Test(expected = HaltException.class)
    public void testMovnIssue7() throws Exception {
        runMipsTest("tests/data/movn-issue-7.s");
    }

    @Test(expected = HaltException.class)
    public void testMovzIssue7() throws Exception {
        runMipsTest("tests/data/movz-issue-7.s");
    }
}
