/* CpuTests.java
 *
 * Tests for the EduMIPS64 CPU.
 *
 * (c) 2012-2013 Andrea Spadaccini
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

import java.util.Map;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;


public class CpuTests {
    protected CPU cpu;
    protected Parser parser;
    public static String testsLocation = "src/tests/data/";

    /** Class that holds the parts of the CPU status that need to be tested
     * after the execution of a test case.
     */
    class CpuTestStatus {
        int cycles;
        int wawStalls;
        int rawStalls;

        public CpuTestStatus(CPU cpu) {
            cycles = cpu.getCycles();
            wawStalls = cpu.getWAWStalls();
            rawStalls = cpu.getRAWStalls();
            System.err.println("Got " + cycles + " cycles, " + rawStalls + " RAW Stalls and " + wawStalls + " WAW stalls.");
        }
    }

    @Before
    public void setUp() {
        cpu = CPU.getInstance();
        cpu.setStatus(CPU.CPUStatus.READY);
        parser = Parser.getInstance();
        Instruction.setEnableForwarding(true);
    }

    /** Executes a MIPS64 program, raising an exception if it does not
     * succeed.
     *
     * @param testPath path of the test code.
     */
    protected CpuTestStatus runMipsTest(String testPath) throws Exception {
        System.err.println("================================= Starting test " + testPath);
        cpu.reset();
        testPath = testsLocation + testPath;
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
            System.err.println("================================= Finished test " + testPath);
            return cts;
        } finally {
            cpu.reset();
        }
    }

    /** Runs a MIPS64 test program with and without forwarding, raising an
     *  exception if it does not succeed.
     *
     * @param testPath path of the test code.
     * @return a dictionary that maps the forwarding status to the
     * corresponding CpuTestStatus object.
     */
    protected Map<Boolean, CpuTestStatus> runMipsTestWithAndWithoutForwarding(String testPath) throws Exception {
        boolean forwardingStatus = Instruction.getEnableForwarding();
        Map<Boolean, CpuTestStatus> statuses = new HashMap<Boolean, CpuTestStatus>();

        Instruction.setEnableForwarding(true);
        statuses.put(true, runMipsTest(testPath));

        Instruction.setEnableForwarding(false);
        statuses.put(false, runMipsTest(testPath));

        Instruction.setEnableForwarding(forwardingStatus);
        return statuses;
    }

    private void runForwardingTest(String path, int cycles_with_forwarding, 
                                   int cycles_without_forwarding) throws Exception {
        Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding(path);

        Assert.assertEquals(cycles_with_forwarding, statuses.get(true).cycles);
        Assert.assertEquals(cycles_without_forwarding, statuses.get(false).cycles);
    }


    /* Test for the instruction BREAK */
    @Test(expected = BreakException.class)
    public void testBREAK() throws Exception {
        runMipsTest("break.s");
    }

    /* Test for r0 */
    @Test
    public void testR0() throws Exception {
        runMipsTest("zero.s");
    }

    /* Test for instruction B */
    @Test
    public void testB() throws Exception {
        runMipsTest("b.s");
    }

    /* Test for the instruction JAL */
    @Test
    public void testJAL() throws Exception {
        runMipsTest("jal.s");
    }

    /* Test for utils/strlen.s */
    @Test
    public void testStrlen() throws Exception {
        runMipsTest("test-strlen.s");
    }

    /* Test for utils/strcmp.s */
    @Test
    public void testStrcmp() throws Exception {
        runMipsTest("test-strcmp.s");
    }

    /* Tests for the memory */
    @Test
    public void testMemory() throws Exception {
        runMipsTest("memtest.s");
    }

    /* Forwarding test. The number of cycles is hardcoded and depends on the
     * contents of forwarding.s */
    @Test
    public void testForwarding() throws Exception {
        CpuTestStatus temp;

        // Simple test.
        runForwardingTest("forwarding.s", 16, 19);
        
        // Tests taken from Hennessy & Patterson, Appendix A
        runForwardingTest("forwarding-hp-pA16.s", 11, 13);
        runForwardingTest("forwarding-hp-pA18.s", 9, 13);
    }

    @Test
    public void storeAfterLoad() throws Exception {
        runMipsTest("store-after-load.s");
    }

    /* ------- FPU TESTS -------- */
    @Test
    public void testFPUStalls() throws Exception {
        Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("fpu-waw.s");

        // With forwarding
        Assert.assertEquals(20, statuses.get(true).cycles);
        Assert.assertEquals(7, statuses.get(true).wawStalls);
        Assert.assertEquals(1, statuses.get(true).rawStalls);

        // Without forwarding
        Assert.assertEquals(21, statuses.get(false).cycles);
        Assert.assertEquals(7, statuses.get(false).wawStalls);
        Assert.assertEquals(2, statuses.get(false).rawStalls);
    }

    /* ------- REGRESSION TESTS -------- */
    /* Issue #7 */
    @Test
    public void testMovnIssue7() throws Exception {
        runMipsTest("movn-issue-7.s");
    }

    @Test
    public void testMovzIssue7() throws Exception {
        runMipsTest("movz-issue-7.s");
    }

    /* Issue #2: Misaligned memory operations are not handled correctly */
    @Test(expected = NotAlignException.class)
    public void testMisalignLD() throws Exception {
        runMipsTest("misaligned-ld.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSD() throws Exception {
        runMipsTest("misaligned-sd.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLW() throws Exception {
        runMipsTest("misaligned-lw.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLWU() throws Exception {
        runMipsTest("misaligned-lwu.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSW() throws Exception {
        runMipsTest("misaligned-sw.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLH() throws Exception {
        runMipsTest("misaligned-lh.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignLHU() throws Exception {
        runMipsTest("misaligned-lhu.s");
    }

    @Test(expected = NotAlignException.class)
    public void testMisalignSH() throws Exception {
        runMipsTest("misaligned-sh.s");
    }

    @Test
    public void testAligned() throws Exception {
        runMipsTest("aligned.s");
    }
}
