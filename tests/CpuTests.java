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
import org.junit.Assert;
import static org.junit.Assert.assertTrue;


public class CpuTests {
    protected CPU cpu;
    protected Parser parser;

    /** Class that holds the parts of the CPU status that need to be tested
     * after the execution of a test case.
     */
    class CpuTestStatus {
        int cycles;

        public CpuTestStatus(CPU cpu) {
            cycles = cpu.getCycles();
        }
    }

    @Before
    public void setUp() {
        cpu = CPU.getInstance();
        cpu.setStatus(CPU.CPUStatus.READY);
        parser = Parser.getInstance();
    }

    /** Runs a MIPS64 test program with and without forwarding, raising an
     *  exception if it does not succeed.
     *
     * @param testPath path of the test code.
     */
    protected void runMipsTest(String testPath) throws Exception {
        Instruction.setEnableForwarding(true);
        executeMipsTest(testPath);

        Instruction.setEnableForwarding(false);
        executeMipsTest(testPath);
    }

    /** Executes a MIPS64 program, raising an exception if it does not
     * succeed.
     *
     * @param testPath path of the test code.
     */
    protected CpuTestStatus executeMipsTest(String testPath) throws Exception {
        try {
            try {
                parser.parse(testPath);
            } catch (ParserMultiWarningException e) {
                // This exception is raised even if there are only warnings.
                // We must raise it only if there are actual errors.
                if(e.hasErrors()) {
                    throw e;
                }
            }
            cpu.setStatus(CPU.CPUStatus.RUNNING);
            while(true) {
                cpu.step();
            }
        }
        catch (HaltException e) {
            CpuTestStatus cts = new CpuTestStatus(cpu);
            return cts;
        } finally {
            cpu.reset();
        }
    }

    /* Test for the instruction BREAK */
    @Test(expected = BreakException.class)
    public void testBREAK() throws Exception {
        runMipsTest("tests/data/break.s");
    }

    /* Test for r0 */
    @Test
    public void testR0() throws Exception {
        runMipsTest("tests/data/zero.s");
    }

    /* Test for instruction B */
    @Test
    public void testB() throws Exception {
        runMipsTest("tests/data/b.s");
    }

    /* Test for the instruction JAL */
    @Test
    public void testJAL() throws Exception {
        runMipsTest("tests/data/jal.s");
    }

    /* Test for utils/strlen.s */
    @Test
    public void testStrlen() throws Exception {
        runMipsTest("tests/data/test-strlen.s");
    }

    /* Test for utils/strcmp.s */
    @Test
    public void testStrcmp() throws Exception {
        runMipsTest("tests/data/test-strcmp.s");
    }

    /* Tests for the memory */
    @Test
    public void testMemory() throws Exception {
        runMipsTest("tests/data/memtest.s");
    }

    private void runForwardingTest(String path, int cycles_with_forwarding, 
                                   int cycles_without_forwarding) throws Exception {
        CpuTestStatus temp;

        Instruction.setEnableForwarding(true);
        temp = executeMipsTest(path);
        Assert.assertEquals(cycles_with_forwarding, temp.cycles);

        Instruction.setEnableForwarding(false);
        temp = executeMipsTest(path);
        Assert.assertEquals(cycles_without_forwarding, temp.cycles);
    }

    /* Forwarding test. The number of cycles is hardcoded and depends on the
     * contents of forwarding.s */
    @Test
    public void testForwarding() throws Exception {
        CpuTestStatus temp;

        // Simple test.
        runForwardingTest("tests/data/forwarding.s", 16, 19);
        
        // Tests taken from Hennessy & Patterson, Appendix A
        runForwardingTest("tests/data/forwarding-hp-pA16.s", 11, 13);
    }

    @Test
    public void storeAfterLoad() throws Exception {
        runMipsTest("tests/data/store-after-load.s");
    }

    /* ------- REGRESSION TESTS -------- */
    /* Issue #7 */
    @Test
    public void testMovnIssue7() throws Exception {
        runMipsTest("tests/data/movn-issue-7.s");
    }

    @Test
    public void testMovzIssue7() throws Exception {
        runMipsTest("tests/data/movz-issue-7.s");
    }

    /* Issue #2: Misaligned memory operations are not handled correctly */
    @Test(expected = NotAlignException.class)
    public void testMisalignLD() throws Exception {
        runMipsTest("tests/data/misaligned-ld.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSD() throws Exception {
        runMipsTest("tests/data/misaligned-sd.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLW() throws Exception {
        runMipsTest("tests/data/misaligned-lw.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLWU() throws Exception {
        runMipsTest("tests/data/misaligned-lwu.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSW() throws Exception {
        runMipsTest("tests/data/misaligned-sw.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLH() throws Exception {
        runMipsTest("tests/data/misaligned-lh.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLHU() throws Exception {
        runMipsTest("tests/data/misaligned-lhu.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSH() throws Exception {
        runMipsTest("tests/data/misaligned-sh.s");
    }

    @Test
    public void testAligned() throws Exception {
        runMipsTest("tests/data/aligned.s");
    }
}
