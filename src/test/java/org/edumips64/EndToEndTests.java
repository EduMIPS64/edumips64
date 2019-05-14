/* EndToEndTests.java
 *
 * Tests for the EduMIPS64 CPU. These test focus on running MIPS64 programs,
 * treating the CPU as a black box and analyzing the correctness of its
 * outputs.
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
package org.edumips64;

import org.edumips64.core.*;
import org.edumips64.core.fpu.RegisterFP;
import org.edumips64.core.is.*;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiWarningException;
import org.edumips64.utils.CycleBuilder;
import org.edumips64.utils.CycleElement;
import org.edumips64.utils.ConfigKey;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.LocalFileUtils;
import org.edumips64.utils.io.LocalWriter;
import org.edumips64.utils.io.StringWriter;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Scanner;

import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EndToEndTests extends BaseWithInstructionBuilderTest {
  private final static Logger log = Logger.getLogger(EndToEndTests.class.getName());
  private Parser parser;
  private CycleBuilder builder;
  private static String testsLocation = "src/test/resources/";

  @Rule
  public ErrorCollector collector = new ErrorCollector();

  /** Class that holds the parts of the CPU status that need to be tested
   * after the execution of a test case.
   */
  class CpuTestStatus {
    int cycles;
    int instructions;
    int rawStalls, wawStalls, memStalls, divStalls;
    String traceFile;
    RegisterFP[] fpRegisters;

    CpuTestStatus(CPU cpu, String dineroTrace) {
      cycles = cpu.getCycles();
      instructions = cpu.getInstructions();
      wawStalls = cpu.getWAWStalls();
      rawStalls = cpu.getRAWStalls();
      memStalls = cpu.getStructuralStallsMemory();
      divStalls = cpu.getStructuralStallsDivider();
      traceFile = dineroTrace;

      // Deep copy the FP Registers.
      RegisterFP cpuFPRegisters[] = cpu.getRegistersFP();
      fpRegisters = new RegisterFP[cpuFPRegisters.length];
      log.info("Deep copying " + cpuFPRegisters.length + " FP registers");
      for (int i = 0; i < cpuFPRegisters.length; ++i) {
        RegisterFP r = cpuFPRegisters[i];
        fpRegisters[i] = new RegisterFP("F" + i);
        try {
          log.info(i + ": " + r.getBinString());
          fpRegisters[i].setBits(r.getBinString(), 0);
        } catch (IrregularStringOfBitsException e) {
          // Should never happen.
          e.printStackTrace();
        }
      }

      log.warning("Got " + cycles + " cycles, " + instructions + " instructions, " + rawStalls + " RAW Stalls and " + wawStalls + " WAW stalls.");
    }
  }

  /** Class to hold the FPU exceptions configuration.
   */
  class FPUExceptionsConfig {
    boolean invalidOperation, overflow, underflow, divideByZero;

    // Constructor, initializes the values from the Config store.
    public FPUExceptionsConfig() {
      invalidOperation = config.getBoolean(ConfigKey.FP_INVALID_OPERATION);
      overflow = config.getBoolean(ConfigKey.FP_OVERFLOW);
      underflow = config.getBoolean(ConfigKey.FP_UNDERFLOW);
      divideByZero = config.getBoolean(ConfigKey.FP_DIVIDE_BY_ZERO);
    }

    // Restore values to the config Store.
    void restore() {
      config.putBoolean(ConfigKey.FP_INVALID_OPERATION, invalidOperation);
      config.putBoolean(ConfigKey.FP_OVERFLOW, overflow);
      config.putBoolean(ConfigKey.FP_UNDERFLOW, underflow);
      config.putBoolean(ConfigKey.FP_DIVIDE_BY_ZERO, divideByZero);
    }
  }

  private FPUExceptionsConfig fec;

  @Before
  public void testSetup() {
    super.testSetup();
    parser  = new Parser(lfu, symTab, memory, instructionBuilder);
    config.putBoolean(ConfigKey.FORWARDING, true);
    fec = new FPUExceptionsConfig();
    builder = new CycleBuilder(cpu);
  }

  @After
  public void testTearDown() {
    fec.restore();
  }

  /** Executes a MIPS64 program, raising an exception if it does not
   * succeed.
   *
   * @param testPath path of the test code.
   */
  private CpuTestStatus runMipsTest(String testPath) throws Exception {
    return runMipsTest(testPath, false);
  }

  // Version of runMipsTest that allows to specify whether to write a trace file.
  // Writing the trace file can unnecessarily slow down the tests, so by default they are not written (see previous
  // override of this method).
  private CpuTestStatus runMipsTest(String testPath, boolean writeTracefile) throws Exception {
    log.warning("================================= Starting test " + testPath + " (forwarding: " +
        config.getBoolean(ConfigKey.FORWARDING) + ")");
    cpu.reset();
    dinero.reset();
    symTab.reset();
    builder.reset();
    testPath = testsLocation + testPath;
    String tracefile = null;

    try {
      try {
        String absoluteFilename = new File(testPath).getAbsolutePath();
        parser.parse(absoluteFilename);
      } catch (ParserMultiWarningException e) {
        // This exception is raised even if there are only warnings.
        // We must raise it only if there are actual errors.
        if (e.hasErrors()) {
          throw e;
        }
      }

      dinero.setDataOffset(memory.getInstructionsNumber()*4);
      cpu.setStatus(CPU.CPUStatus.RUNNING);

      while (true) {
        cpu.step();
        builder.step();
      }
    } catch (HaltException e) {
      log.warning("================================= Finished test " + testPath);
      log.info(cpu.toString());

      if (writeTracefile) {
        File tmp = File.createTempFile("edumips64", "xdin");
        tracefile = tmp.getAbsolutePath();
        tmp.deleteOnExit();
        LocalWriter w = new LocalWriter(tmp.getAbsolutePath(), false);
        dinero.writeTraceData(w);
        w.close();
      }

      // Check if the transactions in the CycleBuilder are all valid.
      boolean allValid = true;
      for (CycleElement el : builder.getElementsList()) {
        allValid &= el.isValid();
      }
      if (!allValid) {
        throw new InvalidCycleElementTransactionException();
      }

      return new CpuTestStatus(cpu, tracefile);
    } finally {
      cpu.reset();
      dinero.reset();
      symTab.reset();
    }
  }

  enum ForwardingStatus {ENABLED, DISABLED}

  /** Runs a MIPS64 test program with and without forwarding, raising an
   *  exception if it does not succeed.
   *
   * @param testPath path of the test code.
   * @return a dictionary that maps the forwarding status to the
   * corresponding CpuTestStatus object.
   */
  private Map<ForwardingStatus, CpuTestStatus> runMipsTestWithAndWithoutForwarding(String testPath) throws Exception {
    boolean forwardingStatus = config.getBoolean(ConfigKey.FORWARDING);
    Map<ForwardingStatus, CpuTestStatus> statuses = new HashMap<>();

    config.putBoolean(ConfigKey.FORWARDING, true);
    statuses.put(ForwardingStatus.ENABLED, runMipsTest(testPath));

    config.putBoolean(ConfigKey.FORWARDING, false);
    statuses.put(ForwardingStatus.DISABLED, runMipsTest(testPath));

    config.putBoolean(ConfigKey.FORWARDING, forwardingStatus);
    return statuses;
  }

  private void runForwardingTest(String path, int expected_cycles_with_forwarding,
                                 int expected_cycles_without_forwarding, int expected_instructions) throws Exception {
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding(path);

    collector.checkThat("Cycles with forwarding (" + path + ")", statuses.get(ForwardingStatus.ENABLED).cycles, equalTo(expected_cycles_with_forwarding));
    collector.checkThat("Cycles without forwarding (" + path + ")", statuses.get(ForwardingStatus.DISABLED).cycles, equalTo(expected_cycles_without_forwarding));
    collector.checkThat("Instructions with forwarding (" + path + ")", statuses.get(ForwardingStatus.ENABLED).instructions, equalTo(expected_instructions));
    collector.checkThat("Instructions without forwarding (" + path + ")", statuses.get(ForwardingStatus.DISABLED).instructions, equalTo(expected_instructions));
  }

  private void runTestAndCompareTracefileWithGolden(String path) throws Exception {
    CpuTestStatus s = runMipsTest(path, true);
    String goldenTrace = testsLocation + path + ".xdin.golden";

    String golden = new Scanner(new File(goldenTrace)).useDelimiter("\\A").next();
    String trace = new Scanner(new File(s.traceFile)).useDelimiter("\\A").next();
    golden = golden.replaceAll("\r\n", "\n");
    trace = trace.replaceAll("\r\n", "\n");
    collector.checkThat("Dinero trace file differs from the golden one.", trace, equalTo(golden));
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

  /* Test instruction and cycle count for the simplest valid program. */
  @Test
  public void testHalt() throws Exception {
      CpuTestStatus status = runMipsTest("halt.s");

      collector.checkThat(status.cycles, equalTo(5));
      collector.checkThat(status.instructions, equalTo(1));
      collector.checkThat(status.memStalls, equalTo(0));
      collector.checkThat(status.rawStalls, equalTo(0));
      collector.checkThat(status.wawStalls, equalTo(0));
  }

  /* Tests for instruction SYSCALL. */
  @Test(expected = BreakException.class)
  public void testOpenNonExistent() throws Exception {
    runMipsTest("test-open-nonexistent.s");
  }

  @Test
  public void testOpenExistent() throws Exception {
    runMipsTest("test-open-existent.s");
  }

  @Test
  public void testReadWriteFile() throws Exception {
    // TODO(andrea): clean up the test file.
    runMipsTest("read-write.s");
  }

  @Test
  public void testPrintf() throws Exception {
    runMipsTest("hello-world.s");
    assertEquals("9th of July:\nEduMIPS64 version 1.2 is being tested! 100% success!", stdOut.toString());
  }

  /* Test for instruction B */
  @Test
  public void testB() throws Exception {
    runMipsTest("b.s");
  }

  /* Test for instruction DADDU */
  @Test
  public void testDADDU() throws Exception {
    runMipsTest("daddu-simple-test.s");
}

  /* Test for instruction DSUBU */
  @Test
  public void testDSUBU() throws Exception {
        runMipsTest("dsubu-simple-test.s");
    }

  /* Test for instructions DMULU and DMULTU */
  @Test
  public void testDMULU() throws Exception {
        runMipsTest("dmulu-simple-test.s");
    }

  /* Test for the instruction JAL */
  @Test
  public void testJAL() throws Exception {
    runMipsTest("jal.s");
  }

  /* Test for instructions DIV, MFLO, MFHI */
  @Test
  public void testDIV() throws Exception {
    runMipsTest("div.s");
  }

  /* Test for instruction DIVU */
  @Test
  public void testDIVU() throws Exception {
    runMipsTest("divu.s");
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

  /* Read-after-write test */
  @Test
  public void testRAW() throws Exception {
    runMipsTestWithAndWithoutForwarding("raw.s");
  }

  /* Forwarding test. The number of cycles is hardcoded and depends on the
   * contents of forwarding.s */
  @Test
  public void testForwarding() throws Exception {
    // Simple test.
    runForwardingTest("forwarding.s", 15, 18, 10);

    // Tests taken from Hennessy & Patterson, Appendix A
    runForwardingTest("forwarding-hp-pA16.s", 10, 12, 6);
    runForwardingTest("forwarding-hp-pA18.s", 8, 12, 4);
  }

  @Test
  public void storeAfterLoad() throws Exception {
    runMipsTest("store-after-load.s");
  }

  /* ------- FPU TESTS -------- */
  @Test
  public void testFPUStalls() throws Exception {
    String filename = "fpu-waw.s";
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding(filename);

    // With forwarding
    collector.checkThat(filename + ": cycles with forwarding.", statuses.get(ForwardingStatus.ENABLED).cycles, equalTo(19));
    collector.checkThat(filename + ": instructions with forwarding.", statuses.get(ForwardingStatus.ENABLED).instructions, equalTo(5));
    collector.checkThat(filename + ": WAW stalls with forwarding." ,statuses.get(ForwardingStatus.ENABLED).wawStalls, equalTo(7));
    collector.checkThat(filename + ": RAW stalls with forwarding.", statuses.get(ForwardingStatus.ENABLED).rawStalls, equalTo(1));

    // Without forwarding
    collector.checkThat(filename + ": cycles without forwarding.", statuses.get(ForwardingStatus.DISABLED).cycles, equalTo(20));
    collector.checkThat(filename + ": instructions without forwarding.", statuses.get(ForwardingStatus.DISABLED).instructions, equalTo(5));
    collector.checkThat(filename + ": WAW stalls without forwarding." ,statuses.get(ForwardingStatus.DISABLED).wawStalls, equalTo(7));
    collector.checkThat(filename + ": RAW stalls without forwarding.", statuses.get(ForwardingStatus.DISABLED).rawStalls, equalTo(2));
  }

  @Test
  public void testFPUMul() throws Exception {
    // This test contains code that raises exceptions, let's disable them.
    config.putBoolean(ConfigKey.FP_INVALID_OPERATION, false);
    config.putBoolean(ConfigKey.FP_OVERFLOW, false);
    config.putBoolean(ConfigKey.FP_UNDERFLOW, false);
    config.putBoolean(ConfigKey.FP_DIVIDE_BY_ZERO, false);
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("fpu-mul.s");

    // Same behaviour with and without forwarding.
    int expected_cycles = 42, expected_instructions = 32, expected_mem_stalls = 6;
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).cycles, equalTo(expected_cycles));
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).instructions, equalTo(expected_instructions));
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).memStalls, equalTo(expected_mem_stalls));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).cycles, equalTo(expected_cycles));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).instructions, equalTo(expected_instructions));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).memStalls, equalTo(expected_mem_stalls));
  }

  @Test
  public void testFPCond() throws Exception {
    runMipsTest("fp-cond.s");
  }

  @Test
  public void testSubtraction() throws Exception {
    runMipsTest("sub.d.s");
  }

  @Test
  public void testDivision() throws Exception {
    runMipsTest("div.d.s");
  }

  @Test
  public void testDividerStalls() throws Exception {
    CpuTestStatus status = runMipsTest("div.d.divider-stalls.s");
    assertEquals(status.divStalls, 23);
  }

  @Test
  public void testOutOfOrder() throws Exception {
    CpuTestStatus status = runMipsTest("fpu-out-of-order-terminate.s");
    assertEquals(2, Integer.parseInt(status.fpRegisters[2].getBinString(), 2));
  }

  /* Tests for masking synchronous exceptions. Termination cannot be tested here since it's in the CPUSwingWorker. */
  @Test(expected = SynchronousException.class)
  public void testDivisionByZeroThrowException() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTest("div0.s");
  }

  @Test
  public void testDivisionByZeroNoThrowException() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
    runMipsTest("div0.s");
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

  /* Issue #28: Dinero tracefile tracks both Load and Store memory accesses as
   * read. */
  @Test
  public void testTracefile() throws Exception {
    runTestAndCompareTracefileWithGolden("tracefile-ld.s");
    runTestAndCompareTracefileWithGolden("tracefile-ldst.s");
    runTestAndCompareTracefileWithGolden("tracefile-noldst.s");
    runTestAndCompareTracefileWithGolden("tracefile-st.s");
  }

  /* Issue #36: StringIndexOutOfBoundsException raised at run-time. */
  @Test(expected = AddressErrorException.class)
  public void testNegativeAddress() throws Exception {
    runMipsTest("negative-address-issue-36.s");
  }

  /* Issue #51: Problem with SYSCALL 0 after branch. */
  @Test
  public void testTerminationInID() throws Exception {
    runForwardingTest("issue51-halt.s", 11, 17, 6);
    runForwardingTest("issue51-syscall0.s", 11, 17, 6);
  }

  /* Issue #68: JR does not respect RAW stalls. */
  @Test
  public void testRAWForRTypeFlowControl() throws Exception {
    runMipsTestWithAndWithoutForwarding("jr-raw.s");
    runMipsTestWithAndWithoutForwarding("jalr-raw.s");
  }
  
  /* Issue #132:The longer the simulation goes on, the lower the simulation rate.
     This tests only part of the bug, since part of it is related to the GUI. */
  @Test
  public void testHailStone() throws Exception {
    runMipsTestWithAndWithoutForwarding("hailstoneenglish.s");
  }
  @Test
  public void testSetBitSort() throws Exception {
    runMipsTestWithAndWithoutForwarding("set-bit-sort.s");
  }

  /* Issue #175: While parsing LW and SW instructions, offset cannot be negative
     or above 8192 */
  @Test
  public void testLargeOffsets() throws Exception {
    runMipsTestWithAndWithoutForwarding("large-offsets.s");
  }
  @Test
  public void testNegativeOffsets() throws Exception {
    runMipsTestWithAndWithoutForwarding("negative-offsets.s");
  }
}
