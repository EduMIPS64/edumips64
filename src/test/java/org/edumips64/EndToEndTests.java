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
import org.edumips64.core.cache.CacheConfig;
import org.edumips64.core.cache.CacheStats;
import org.edumips64.core.fpu.RegisterFP;
import org.edumips64.core.is.AddressErrorException;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InvalidDelaySlotException;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.CycleBuilder;
import org.edumips64.utils.CycleElement;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.io.LocalWriter;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    long[] gprValues;

    CpuTestStatus(CPU cpu, String dineroTrace) {
      cycles = cpu.getCycles();
      instructions = cpu.getInstructions();
      wawStalls = cpu.getWAWStalls();
      rawStalls = cpu.getRAWStalls();
      memStalls = cpu.getStructuralStallsMemory();
      divStalls = cpu.getStructuralStallsDivider();
      traceFile = dineroTrace;

      // Snapshot GPRs as integer values so that tests can verify register
      // state after `runMipsTest()`'s `finally` block has reset the CPU.
      gprValues = new long[32];
      for (int i = 0; i < 32; ++i) {
        gprValues[i] = cpu.getRegister(i).getValue();
      }

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
        config.getBoolean(ConfigKey.FORWARDING)+ ")");
    cpu.reset();
    cachesim.reset();
    symTab.reset();
    builder.reset();
    testPath = testsLocation + testPath;
    String tracefile = null;

    try {
      try {
        String absoluteFilename = new File(testPath).getAbsolutePath();
        parser.parse(absoluteFilename);
      } catch (ParserMultiException e) {
        // This exception may be raised even if there are only warnings.
        // We must raise it only if there are actual errors.
        if (e.hasErrors()) {
          throw e;
        }
      }

      cachesim.setDataOffset(memory.getInstructionsNumber()*4);
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
        cachesim.writeTraceData(w);
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
    CpuTestStatus status = runMipsTest(path, true);
    String goldenTrace = testsLocation + path + ".xdin.golden";

    String golden, trace;

    try (
      Scanner goldenScanner = new Scanner(new File(goldenTrace));
      Scanner traceScanner = new Scanner(new File(status.traceFile));
    ) {
      golden = goldenScanner.useDelimiter("\\A").next().replaceAll("\r\n", "\n");
      trace = traceScanner.useDelimiter("\\A").next().replaceAll("\r\n", "\n");
      collector.checkThat("Dinero trace file differs from the golden one.", trace, equalTo(golden));
    }
  }

  /* Test for the instruction BREAK */
  @Test(expected = BreakException.class, timeout=2000)
  public void testBREAK() throws Exception {
    runMipsTest("break.s");
  }

  /* Test for r0 */
  @Test(timeout=2000)
  public void testR0() throws Exception {
    runMipsTest("zero.s");
  }

  /* Test instruction and cycle count for the simplest valid program. */
  @Test(timeout=2000)
  public void testHalt() throws Exception {
      CpuTestStatus status = runMipsTest("halt.s");

      collector.checkThat(status.cycles, equalTo(5));
      collector.checkThat(status.instructions, equalTo(1));
      collector.checkThat(status.memStalls, equalTo(0));
      collector.checkThat(status.rawStalls, equalTo(0));
      collector.checkThat(status.wawStalls, equalTo(0));
  }

  /* Tests for instruction SYSCALL. */
  @Test(expected = BreakException.class, timeout=2000)
  public void testOpenNonExistent() throws Exception {
    runMipsTest("test-open-nonexistent.s");
  }

  @Test(timeout=2000)
  public void testOpenExistent() throws Exception {
    runMipsTest("test-open-existent.s");
  }

  @Test(timeout=2000)
  public void testReadWriteFile() throws Exception {
    // TODO(andrea): clean up the test file.
    runMipsTest("read-write.s");
  }

  @Test(timeout=2000)
  public void testPrintf() throws Exception {
    runMipsTest("hello-world.s");
    assertEquals("9th of July:\nEduMIPS64 version 1.2 is being tested! 100% success!", stdOut.toString());
  }

  /* Test for instruction B */
  @Test(timeout=2000)
  public void testB() throws Exception {
    runMipsTest("b.s");
  }

  /* Test for instruction DADDU */
  @Test(timeout=2000)
  public void testDADDU() throws Exception {
    runMipsTest("daddu-simple-test.s");
}

  /* Test for instruction DSUBU */
  @Test(timeout=2000)
  public void testDSUBU() throws Exception {
        runMipsTest("dsubu-simple-test.s");
    }

  /* Test for instructions DMULU and DMULTU */
  @Test(timeout=2000)
  public void testDMULU() throws Exception {
        runMipsTest("dmulu-simple-test.s");
    }

  /* Test for instructions DMUL and DDIV */
  @Test(timeout=2000)
  public void testDMULandDDIV() throws Exception {
        runMipsTest("dmul-ddiv-test.s");
    }

  /* Test for both DDIV forms (2-param and 3-param) */
  @Test(timeout=2000)
  public void testDDIVBothForms() throws Exception {
        runMipsTest("ddiv-both-forms-test.s");
    }

  /* Test for instruction DMOD */
  @Test(timeout=2000)
  public void testDMOD() throws Exception {
        runMipsTest("dmod-test.s");
    }

  /* Test for the instruction JAL */
  @Test(timeout=2000)
  public void testJAL() throws Exception {
    runMipsTest("jal.s");
  }

  /* Test for instructions DIV, MFLO, MFHI */
  @Test(timeout=2000)
  public void testDIV() throws Exception {
    runMipsTest("div.s");
  }

  /* Test for instruction DIVU */
  @Test(timeout=2000)
  public void testDIVU() throws Exception {
    runMipsTest("divu.s");
  }

  /* Test for instruction ADD */
  @Test(timeout=2000)
  public void testAdd() throws Exception {
    runMipsTest("add.s");
  }

  /* Test for instruction BC1F */
  @Test(timeout=2000)
  public void testBc1f() throws Exception {
    runMipsTest("bc1f.s");
  }

  /* Test for instruction BGEZ */
  @Test(timeout=2000)
  public void testBgez() throws Exception {
    runMipsTest("bgez.s");
  }

  /* Test for instruction C.LT.D */
  @Test(timeout=2000)
  public void testCltd() throws Exception {
    runMipsTest("cltd.s");
  }

  /* Test for instruction CVT.D.W */
  @Test(timeout=2000)
  public void testCvtdw() throws Exception {
    runMipsTest("cvtdw.s");
  }

  /* Test for instruction SLT */
  @Test(timeout=2000)
  public void testSlt() throws Exception {
    runMipsTest("slt.s");
  }

  /* Test for instruction SUB */
  @Test(timeout=2000)
  public void testSub() throws Exception {
    runMipsTest("sub.s");
  }

  /* Test for instruction SRL */
  @Test(timeout=2000)
  public void testSrl() throws Exception {
    runMipsTest("srl.s");
  }

  /* Test for instruction SRLV */
  @Test(timeout=2000)
  public void testSrlv() throws Exception {
    runMipsTest("srlv.s");
  }

  /* Test for instruction SLL */
  @Test(timeout=2000)
  public void testSll() throws Exception {
    runMipsTest("sll.s");
  }

  /* Test for instruction SLLV */
  @Test(timeout=2000)
  public void testSllv() throws Exception {
    runMipsTest("sllv.s");
  }

  /* Test for instruction ADDIU */
  @Test(timeout=2000)
  public void testAddiu() throws Exception {
    runMipsTest("addiu.s");
  }

  /* Test for instruction ADDU */
  @Test(timeout=2000)
  public void testAddu() throws Exception {
    runMipsTest("addu.s");
  }

  /* Test for instruction SUBU */
  @Test(timeout=2000)
  public void testSubu() throws Exception {
    runMipsTest("subu.s");
  }

  /* Test for instruction SLTI */
  @Test(timeout=2000)
  public void testSlti() throws Exception {
    runMipsTest("slti.s");
  }

  /* Test for instruction SLTIU */
  @Test(timeout=2000)
  public void testSltiu() throws Exception {
    runMipsTest("sltiu.s");
  }

  /* Test for instruction SLTU */
  @Test(timeout=2000)
  public void testSltu() throws Exception {
    runMipsTest("sltu.s");
  }

  /* Test for instruction DADDIU */
  @Test(timeout=2000)
  public void testDaddiu() throws Exception {
    runMipsTest("daddiu.s");
  }

  /* Test for instruction DADDUI (alias of DADDIU) */
  @Test(timeout=2000)
  public void testDaddui() throws Exception {
    runMipsTest("daddui.s");
  }

  /* Test for instruction CVT.W.D */
  @Test(timeout=2000)
  public void testCvtwd() throws Exception {
    runMipsTest("cvtwd.s");
  }

  /* Test for instruction MFC1 */
  @Test(timeout=2000)
  public void testMfc1() throws Exception {
    runMipsTest("mfc1.s");
  }

  /* Test for instruction MOV.D */
  @Test(timeout=2000)
  public void testMovd() throws Exception {
    runMipsTest("movd.s");
  }

  /* Test for instruction MOVF.D */
  @Test(timeout=2000)
  public void testMovfd() throws Exception {
    runMipsTest("movfd.s");
  }

  /* Regression test for the MOVF.D/MOVT.D write-semaphore leak: with
   * forwarding enabled, FPConditionalCC_DMoveInstructions used to skip
   * doWB() in EX, so the destination register's write semaphore was
   * never released and any later reader would stall forever. */
  @Test(timeout=2000)
  public void testMovfdSemaphoreLeak() throws Exception {
    runMipsTest("movfd-semaphore-leak.s");
  }

  /* Test for instruction MOVT.D */
  @Test(timeout=2000)
  public void testMovtd() throws Exception {
    runMipsTest("movtd.s");
  }

  /* Test for instruction MOVN.D */
  @Test(timeout=2000)
  public void testMovnd() throws Exception {
    runMipsTest("movnd.s");
  }

  /* Test for instruction MOVZ.D */
  @Test(timeout=2000)
  public void testMovzd() throws Exception {
    runMipsTest("movzd.s");
  }

  /* Test for instruction S.D */
  @Test(timeout=2000)
  public void testSdFp() throws Exception {
    runMipsTest("sd-fp.s");
  }

  /* Test for instructions LWC1 and SWC1 */
  @Test(timeout=2000)
  public void testLwc1Swc1() throws Exception {
    runMipsTest("lwc1-swc1.s");
  }

  /* Test for instruction TRAP (alias for SYSCALL) */
  @Test(timeout=2000)
  public void testTrap() throws Exception {
    runMipsTest("trap.s");
  }


  /* Test for utils/strlen.s */
  @Test(timeout=2000)
  public void testStrlen() throws Exception {
    runMipsTest("test-strlen.s");
  }

  /* Test for utils/strcmp.s */
  @Test(timeout=2000)
  public void testStrcmp() throws Exception {
    runMipsTest("test-strcmp.s");
  }

  /* Tests for the memory */
  @Test(timeout=2000)
  public void testMemory() throws Exception {
    runMipsTest("memtest.s");
  }

  /* Read-after-write test */
  @Test(timeout=2000)
  public void testRAW() throws Exception {
    runMipsTestWithAndWithoutForwarding("raw.s");
  }

  /* Forwarding test. The number of cycles is hardcoded and depends on the
   * contents of forwarding.s */
  @Test(timeout=2000)
  public void testForwarding() throws Exception {
    // Simple test.
    runForwardingTest("forwarding.s", 15, 18, 10);

    // Tests taken from Hennessy & Patterson, Appendix A
    runForwardingTest("forwarding-hp-pA16.s", 10, 12, 6);
    runForwardingTest("forwarding-hp-pA18.s", 8, 12, 4);
  }

  /* Patti forwarding analysis test.
   * Tests all major forwarding scenarios based on Prof. Patti's analysis.
   * Expected RAW stalls:
   *   With forwarding:    TEST1: 10 + TEST2: 0 + TEST3: 0 + TEST4: 1 + TEST5: 2 = 13
   *   Without forwarding: TEST1: 20 + TEST2: 2 + TEST3: 2 + TEST4: 2 + TEST5: 2 = 28
   */
  @Test(timeout=2000)
  public void testPattiForwarding() throws Exception {
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("patti-forwarding.s");

    // With forwarding:
    // TEST1 (ALU→Branch): 1 RAW stall per iteration × 10 iterations = 10
    // TEST2 (ALU→Store):  0 stalls (store defers RT read to MEM)
    // TEST3 (ALU→ALU):    0 stalls (EX→EX forwarding)
    // TEST4 (Load→ALU):   1 stall (load-use hazard)
    // TEST5 (Load→Branch): 2 stalls (load-use + branch-in-ID combined)
    // Total: 13 RAW stalls
    collector.checkThat("patti-forwarding.s: RAW stalls with forwarding.",
        statuses.get(ForwardingStatus.ENABLED).rawStalls, equalTo(13));

    // Without forwarding:
    // TEST1 (ALU→Branch): 2 RAW stalls per iteration × 10 iterations = 20
    // TEST2 (ALU→Store):  2 stalls
    // TEST3 (ALU→ALU):    2 stalls
    // TEST4 (Load→ALU):   2 stalls
    // TEST5 (Load→Branch): 2 stalls
    // Total: 28 RAW stalls
    collector.checkThat("patti-forwarding.s: RAW stalls without forwarding.",
        statuses.get(ForwardingStatus.DISABLED).rawStalls, equalTo(28));
  }

  @Test(timeout=2000)
  public void storeAfterLoad() throws Exception {
    runMipsTest("store-after-load.s");
  }

  /* Issue #702 regression test: an ALU instruction immediately followed by a
   * branch that reads its result must produce a 1-cycle stall even when
   * forwarding is enabled, because branches resolve in the ID stage and
   * there is no EX -> ID forwarding path. */
  @Test(timeout=2000)
  public void testIssue702SltBeqz() throws Exception {
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("issue-702-slt-beqz.s");

    // With forwarding: `slt` writes r1 in EX, `beqz` reads r1 in ID.
    // No EX -> ID forwarding path -> 1 RAW stall.
    collector.checkThat("issue-702-slt-beqz.s: RAW stalls with forwarding.",
        statuses.get(ForwardingStatus.ENABLED).rawStalls, equalTo(1));

    // Without forwarding: branch waits for `slt` to reach WB -> 2 RAW stalls.
    collector.checkThat("issue-702-slt-beqz.s: RAW stalls without forwarding.",
        statuses.get(ForwardingStatus.DISABLED).rawStalls, equalTo(2));
  }

  /* Issue #702 regression test (load-use + branch-in-ID combined): a load
   * immediately followed by a branch that reads its result must produce
   * 2 RAW stalls both with and without forwarding. With forwarding, the
   * load produces the value in MEM, but the branch needs it in ID and
   * there is no MEM -> ID forwarding path: the branch must wait for the
   * value to become available via WB -> ID one cycle later. */
  @Test(timeout=2000)
  public void testIssue702LdBeq() throws Exception {
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("issue-702-ld-beq.s");

    collector.checkThat("issue-702-ld-beq.s: RAW stalls with forwarding.",
        statuses.get(ForwardingStatus.ENABLED).rawStalls, equalTo(2));

    collector.checkThat("issue-702-ld-beq.s: RAW stalls without forwarding.",
        statuses.get(ForwardingStatus.DISABLED).rawStalls, equalTo(2));
  }

  /* ------- FPU TESTS -------- */
  @Test(timeout=2000)
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


  @Test(timeout=2000)
  public void testFPCond() throws Exception {
    runMipsTest("fp-cond.s");
  }

  @Test(timeout=2000)
  public void testSubtraction() throws Exception {
    runMipsTest("sub.d.s");
  }

  @Test(timeout=2000)
  public void testDivision() throws Exception {
    runMipsTestWithAndWithoutForwarding("div.d.s");
  }

  @Test(timeout=2000)
  public void testDividerStalls() throws Exception {
    CpuTestStatus status = runMipsTest("div.d.divider-stalls.s");
    assertEquals(status.divStalls, 23);
  }

  @Test(timeout=2000)
  public void testOutOfOrder() throws Exception {
    CpuTestStatus status = runMipsTest("fpu-out-of-order-terminate.s");
    assertEquals(2, Integer.parseInt(status.fpRegisters[2].getBinString(), 2));
  }

  /* Tests for masking synchronous exceptions. Termination cannot be tested here since it's in the CPUSwingWorker. */
  @Test(expected = SynchronousException.class, timeout=2000)
  public void testDivisionByZeroThrowException() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTest("div0.s");
  }

  /* Verify that synchronous exceptions carry information about the
   * instruction that caused them and the pipeline stage they were raised in. */
  @Test(timeout=2000)
  public void testSynchronousExceptionHasInstructionAndStageInfo() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    try {
      runMipsTest("div0.s");
      fail("Expected SynchronousException to be thrown");
    } catch (SynchronousException e) {
      assertEquals(SynchronousExceptionCode.DIVZERO, e.getCode());
      assertNotNull("Instruction name should be set on the exception", e.getInstructionName());
      assertEquals("EX", e.getStage());
      // Sanity check: the enriched message should reference the stage.
      assertTrue(
          "Exception message should mention the pipeline stage, got: " + e.getMessage(),
          e.getMessage().contains("EX"));
    }
  }

  /* Test for Out Of Memory while loading a file with more data than the memory can fit. */
  @Test(expected = ParserMultiException.class, timeout=2000)
  public void testOom() throws Exception {
    runMipsTest("oom.s");
  }

  @Test(timeout=2000)
  public void testDivisionByZeroNoThrowException() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
    runMipsTest("div0.s");
  }

  /* Tests for division by zero with 3-parameter DDIV form */
  @Test(expected = SynchronousException.class, timeout=2000)
  public void testDivisionByZeroThrowException3Param() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTest("ddiv3-div0.s");
  }

  @Test(timeout=2000)
  public void testDivisionByZeroNoThrowException3Param() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
    runMipsTest("ddiv3-div0.s");
  }

  /* ------- REGRESSION TESTS -------- */
  /* Issue #7 */
  @Test(timeout=2000)
  public void testMovnIssue7() throws Exception {
    runMipsTest("movn-issue-7.s");
  }

  @Test(timeout=2000)
  public void testMovzIssue7() throws Exception {
    runMipsTest("movz-issue-7.s");
  }

  /* Issue #2: Misaligned memory operations are not handled correctly */
  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignLD() throws Exception {
    runMipsTest("misaligned-ld.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignSD() throws Exception {
    runMipsTest("misaligned-sd.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignLW() throws Exception {
    runMipsTest("misaligned-lw.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignLWU() throws Exception {
    runMipsTest("misaligned-lwu.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignSW() throws Exception {
    runMipsTest("misaligned-sw.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignLH() throws Exception {
    runMipsTest("misaligned-lh.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignLHU() throws Exception {
    runMipsTest("misaligned-lhu.s");
  }

  @Test(expected = NotAlignException.class, timeout=2000)
  public void testMisalignSH() throws Exception {
    runMipsTest("misaligned-sh.s");
  }

  @Test(timeout=2000)
  public void testAligned() throws Exception {
    runMipsTest("aligned.s");
  }

  /* Issue #28: Dinero tracefile tracks both Load and Store memory accesses as
   * read. */
  @Test(timeout=2000)
  public void testTracefile() throws Exception {
    runTestAndCompareTracefileWithGolden("tracefile-ld.s");
    runTestAndCompareTracefileWithGolden("tracefile-ldst.s");
    runTestAndCompareTracefileWithGolden("tracefile-noldst.s");
    runTestAndCompareTracefileWithGolden("tracefile-st.s");
  }


  // Check that cache stats generated during exectution are correct are coherent with
  // those generated when running a tracefile
  @Test
  public void testCacheSimStats() throws Exception {

    String tracefile = "sample.s.xdin";

    Map<CacheConfig, CacheStats> l1iGoldenStats = null;
    Map<CacheConfig, CacheStats> l1dGoldenStats = null;

    l1iGoldenStats = CacheSimulatorTests.loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1I.csv");
    l1dGoldenStats = CacheSimulatorTests.loadStatsFromCSV(testsLocation+tracefile+"_golden_stats_L1D.csv");

    var l1i_cache = cachesim.getL1InstructionCache();
    var l1d_cache = cachesim.getL1DataCache();

    for (Map.Entry<CacheConfig, CacheStats> entry : l1iGoldenStats.entrySet()) {
      CacheConfig config = entry.getKey();
      CacheStats expected = entry.getValue();
      l1i_cache.setConfig(config);
      runMipsTest("sample.s");
      var actual_l1i = cachesim.getL1InstructionCache().getStats();

      collector.checkThat("L1I cache mismatch for config " + config, actual_l1i, equalTo(expected));
    }

    for (Map.Entry<CacheConfig, CacheStats> entry : l1dGoldenStats.entrySet()) {
      CacheConfig config = entry.getKey();
      CacheStats expected = entry.getValue();
      l1d_cache.setConfig(config);
      runMipsTest("sample.s");
      var actual_l1d = cachesim.getL1DataCache().getStats();

      collector.checkThat("L1D cache mismatch for config " + config, actual_l1d, equalTo(expected));
    }
  }

  /* Issue #36: StringIndexOutOfBoundsException raised at run-time. */
  @Test(expected = AddressErrorException.class, timeout=2000)
  public void testNegativeAddress() throws Exception {
    runMipsTest("negative-address-issue-36.s");
  }

  /* Issue #51: Problem with SYSCALL 0 after branch. */
  @Test(timeout=2000)
  public void testTerminationInID() throws Exception {
    runForwardingTest("issue51-halt.s", 13, 17, 6);
    runForwardingTest("issue51-syscall0.s", 13, 17, 6);
  }

  /* Issue #68: JR does not respect RAW stalls. */
  @Test(timeout=2000)
  public void testRAWForRTypeFlowControl() throws Exception {
    runMipsTestWithAndWithoutForwarding("jr-raw.s");
    runMipsTestWithAndWithoutForwarding("jalr-raw.s");
  }

  /* Issue #132:The longer the simulation goes on, the lower the simulation rate.
     This tests only part of the bug, since part of it is related to the GUI. */
  @Test(timeout=20000)
  public void testHailStone() throws Exception {
    runMipsTestWithAndWithoutForwarding("hailstoneenglish.s");
  }
  @Test(timeout=20000)
  public void testSetBitSort() throws Exception {
    runMipsTestWithAndWithoutForwarding("set-bit-sort.s");
  }

  /* Issue #175: While parsing LW and SW instructions, offset cannot be negative
     or above 8192 */
  @Test(timeout=2000)
  public void testLargeOffsets() throws Exception {
    runMipsTestWithAndWithoutForwarding("large-offsets.s");
  }
  @Test(timeout=2000)
  public void testNegativeOffsets() throws Exception {
    runMipsTestWithAndWithoutForwarding("negative-offsets.s");
  }

  /* Issue #255: Trying to store a large memory location in an immediate field
     causes EduMIPS64 to crash */
  @Test(expected = ParserMultiException.class, timeout=2000)
  public void testImmediateOverflow() throws Exception {
    runMipsTest("immediate-overflow.s");
  }

  /* Still issue #255 -- make sure that we can load large memory locations
     via ld/st instructions */
  @Test(timeout=2000)
  public void testLdLargeImmediate() throws Exception {
    runMipsTest("load-large-memory-location.s");
  }

  /* Issue #304: Infinite RAW stall in floating-point.
  */
  @Test(timeout=2000)
  public void testIssue304() throws Exception {
    runMipsTest("issue304.s", false);
  }

  /* Issue #304: Missing MEM/WB in Cycles UI for some FPU instructions.
  */
  @Test(timeout=2000)
  public void testIssue304UI() throws Exception {
    runMipsTest("infinite-bug-304.s", false);
  }

  /* Issue #304: Infinite RAW stall in floating-point due to instruction overwrite in the EX stage.
  */
  @Test(timeout=2000)
  public void testIssue304EX() throws Exception {
    runMipsTest("issue-304-ex.s", false);
  }
  
  /* Issue #304: Missing MEM/WB in Cycles UI for some FPU instructions.
  */
  @Test(timeout=2000)
  public void testIssue646() throws Exception {
    runMipsTest("issue-646-twodiv.s", false);
  }

  /* Test for instruction DDIVU */
  @Test(timeout=2000)
  public void testDdivu() throws Exception {
    runMipsTest("ddivu.s");
  }

  /* Test for instruction DMULT */
  @Test(timeout=2000)
  public void testDmult() throws Exception {
    runMipsTest("dmult.s");
  }

  /* Test for instruction DMULTU */
  @Test(timeout=2000)
  public void testDmultu() throws Exception {
    runMipsTest("dmultu.s");
  }

    /* Test for instructions DMULU and DMUHU */
  @Test(timeout=2000)
  public void testDmuluDmuhu() throws Exception {
    runMipsTest("dmulu-dmuhu.s");
  }

  /* Test for instruction DSLLV */
  @Test(timeout=2000)
  public void testDsllv() throws Exception {
    runMipsTest("dsllv.s");
  }

  /* Test for instruction DSRA */
  @Test(timeout=2000)
  public void testDsra() throws Exception {
    runMipsTest("dsra.s");
  }

  /* Test for instruction DSRLV */
  @Test(timeout=2000)
  public void testDsrlv() throws Exception {
    runMipsTest("dsrlv.s");
  }

  /* Test for instruction MULT */
  @Test(timeout=2000)
  public void testMult() throws Exception {
    runMipsTest("mult.s");
  }

  /* Test for instruction MULTU */
  @Test(timeout=2000)
  public void testMultu() throws Exception {
    runMipsTest("multu.s");
  }
  
  /* Test for instruction ORI */
  @Test(timeout=2000)
  public void testOri() throws Exception {
    runMipsTest("ori.s");
  }

  /* Test for instruction LUI */
  @Test(timeout=2000)
  public void testLui() throws Exception {
    runMipsTest("lui.s");
  }

  /* Test for instruction SRA */
  @Test(timeout=2000)
  public void testSra() throws Exception {
    runMipsTest("sra.s");
  }

  /* Test for instruction SRAV */
  @Test(timeout=2000)
  public void testSrav() throws Exception {
    runMipsTest("srav.s");
  }

  /* Test for instruction XORI */
  @Test(timeout=2000)
  public void testXori() throws Exception {
    runMipsTest("xori.s");
  }

  /* Test for circular #include detection (issue with inclusion loop not being detected) */
  @Test(expected = ParserMultiException.class, timeout=2000)
  public void testCircularInclude() throws Exception {
    runMipsTest("include-1.s");
  }

  /* Test for indirect circular #include detection (file1 -> file2 -> file3 -> file1) */
  @Test(expected = ParserMultiException.class, timeout=2000)
  public void testIndirectCircularInclude() throws Exception {
    runMipsTest("include-indirect-1.s");
  }

  // ------------------------------------------------------------------
  // Branch delay slot tests.
  // ------------------------------------------------------------------

  /** Convenience helper: runs the given test program with the delay slot
   *  disabled and then with it enabled, restoring the original setting on
   *  exit. Returns the two resulting CpuTestStatus instances. */
  private Map<Boolean, CpuTestStatus> runMipsTestWithAndWithoutDelaySlot(String testPath) throws Exception {
    boolean previous = config.getBoolean(ConfigKey.DELAY_SLOT);
    Map<Boolean, CpuTestStatus> statuses = new HashMap<>();
    try {
      config.putBoolean(ConfigKey.DELAY_SLOT, false);
      statuses.put(Boolean.FALSE, runMipsTest(testPath));
      config.putBoolean(ConfigKey.DELAY_SLOT, true);
      statuses.put(Boolean.TRUE, runMipsTest(testPath));
    } finally {
      config.putBoolean(ConfigKey.DELAY_SLOT, previous);
    }
    return statuses;
  }

  /** With delay slot OFF, the instruction immediately after a taken branch
   *  must NOT take effect (it is squashed). With delay slot ON, it must
   *  execute exactly once. The second instruction after the branch must
   *  never execute (it lives past the branch target). */
  @Test(timeout=5000)
  public void testDelaySlotTakenBranch() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-branch.s");
    collector.checkThat("R2 without delay slot",
        statuses.get(false).gprValues[2], equalTo(0L));
    collector.checkThat("R2 with delay slot",
        statuses.get(true).gprValues[2], equalTo(1L));
    // R3 is only touched past the branch target, so it must stay 0 in both
    // configurations.
    collector.checkThat("R3 without delay slot",
        statuses.get(false).gprValues[3], equalTo(0L));
    collector.checkThat("R3 with delay slot",
        statuses.get(true).gprValues[3], equalTo(0L));
  }

  /** Same as above but for an unconditional jump (J), which always squashes
   *  the sequentially-fetched instruction unless the delay slot is enabled. */
  @Test(timeout=5000)
  public void testDelaySlotUnconditionalJump() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-jump.s");
    collector.checkThat("R2 without delay slot",
        statuses.get(false).gprValues[2], equalTo(0L));
    collector.checkThat("R2 with delay slot",
        statuses.get(true).gprValues[2], equalTo(7L));
    collector.checkThat("R3 without delay slot",
        statuses.get(false).gprValues[3], equalTo(0L));
    collector.checkThat("R3 with delay slot",
        statuses.get(true).gprValues[3], equalTo(0L));
  }

  /** For a NOT-taken branch the fall-through instruction is also the "delay
   *  slot" — it must execute regardless of the setting. This guards against
   *  regressions where the delay-slot pipeline rewiring accidentally drops
   *  the next instruction when the branch is not taken. */
  @Test(timeout=5000)
  public void testDelaySlotNotTakenBranch() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-not-taken.s");
    collector.checkThat("R2 without delay slot",
        statuses.get(false).gprValues[2], equalTo(5L));
    collector.checkThat("R2 with delay slot",
        statuses.get(true).gprValues[2], equalTo(5L));
  }

  /** Smoke test for the unconditional-jump (J) family with the delay slot
   *  enabled: ensures a jump and its delay slot continue to produce a clean
   *  run with the delay slot on. JAL register-linking is covered separately by
   *  {@link #testDelaySlotJalLink()}. The test program terminates cleanly via
   *  SYSCALL, so a clean run with the delay slot on is the assertion. */
  @Test(timeout=5000)
  public void testJumpWithDelaySlotSmoke() throws Exception {
    boolean previous = config.getBoolean(ConfigKey.DELAY_SLOT);
    try {
      config.putBoolean(ConfigKey.DELAY_SLOT, true);
      // delay-slot-jump.s uses an unconditional J whose delay slot executes
      // cleanly with the delay slot enabled. (jal.s is unsuitable here because
      // it has `b error` right after `jal continue`; with the delay slot on
      // that `b error` would become the delay slot and jump to `error: break`.)
      runMipsTest("delay-slot-jump.s");
    } finally {
      config.putBoolean(ConfigKey.DELAY_SLOT, previous);
    }
  }

  // ------------------------------------------------------------------
  // Branch delay slot — edge cases.
  //
  // These tests pin down EduMIPS64's behavior for the corner cases of
  // the branch delay slot, based on the rules in the MIPS64
  // Architecture For Programmers Volume II-A (control-transfer in slot,
  // exceptions in slot). Each test runs the same program with the delay slot
  // disabled and enabled and asserts the resulting architectural state.
  // ------------------------------------------------------------------

  /** Convenience helper: enable the delay slot, run the test program,
   *  and restore the previous setting on exit. */
  private CpuTestStatus runMipsTestWithDelaySlot(String testPath) throws Exception {
    boolean previous = config.getBoolean(ConfigKey.DELAY_SLOT);
    try {
      config.putBoolean(ConfigKey.DELAY_SLOT, true);
      return runMipsTest(testPath);
    } finally {
      config.putBoolean(ConfigKey.DELAY_SLOT, previous);
    }
  }

  /** Convenience helper: disable the delay slot, run the test program,
   *  and restore the previous setting on exit. */
  private CpuTestStatus runMipsTestWithoutDelaySlot(String testPath) throws Exception {
    boolean previous = config.getBoolean(ConfigKey.DELAY_SLOT);
    try {
      config.putBoolean(ConfigKey.DELAY_SLOT, false);
      return runMipsTest(testPath);
    } finally {
      config.putBoolean(ConfigKey.DELAY_SLOT, previous);
    }
  }

  /** Branch in the delay slot of another branch. MIPS classifies this as
   *  UNPREDICTABLE. With the delay slot disabled the slot is squashed so
   *  the program completes normally; with the delay slot enabled
   *  EduMIPS64 must raise an InvalidDelaySlotException so the offending
   *  program is diagnosed instead of producing implementation-defined
   *  state. */
  @Test(timeout=5000)
  public void testDelaySlotBranchInSlotDisabled() throws Exception {
    // With delay slot disabled, the inner BEQ in the slot is squashed.
    // Control reaches target1 → syscall 0 → clean termination.
    runMipsTestWithoutDelaySlot("delay-slot-branch-in-slot.s");
  }

  @Test(expected = InvalidDelaySlotException.class, timeout=5000)
  public void testDelaySlotBranchInSlotEnabled() throws Exception {
    runMipsTestWithDelaySlot("delay-slot-branch-in-slot.s");
  }

  /** Unconditional jump in the delay slot of another unconditional jump.
   *  Same classification as the branch-in-branch case. */
  @Test(timeout=5000)
  public void testDelaySlotJumpInSlotDisabled() throws Exception {
    runMipsTestWithoutDelaySlot("delay-slot-jump-in-slot.s");
  }

  @Test(expected = InvalidDelaySlotException.class, timeout=5000)
  public void testDelaySlotJumpInSlotEnabled() throws Exception {
    runMipsTestWithDelaySlot("delay-slot-jump-in-slot.s");
  }

  /** JR in the delay slot of a JAL — the classic "tail call via jr in
   *  delay slot" pattern. UNPREDICTABLE on real MIPS. */
  @Test(timeout=5000)
  public void testDelaySlotJrInJalSlotDisabled() throws Exception {
    runMipsTestWithoutDelaySlot("delay-slot-jr-in-jal-slot.s");
  }

  @Test(expected = InvalidDelaySlotException.class, timeout=5000)
  public void testDelaySlotJrInJalSlotEnabled() throws Exception {
    runMipsTestWithDelaySlot("delay-slot-jr-in-jal-slot.s");
  }

  /** JAL followed by an unconditional branch as the slot. UNPREDICTABLE
   *  on real MIPS; same expected outcome as the cases above. With the
   *  delay slot disabled the slot branch is squashed and the program
   *  completes via the subroutine's syscall. */
  @Test(timeout=5000)
  public void testDelaySlotJalThenBranchDisabled() throws Exception {
    runMipsTestWithoutDelaySlot("delay-slot-jal-then-branch.s");
  }

  @Test(expected = InvalidDelaySlotException.class, timeout=5000)
  public void testDelaySlotJalThenBranchEnabled() throws Exception {
    runMipsTestWithDelaySlot("delay-slot-jal-then-branch.s");
  }

  /** Not-taken loop. With delay slot OFF, the slot (`daddi r3, r3, 7`)
   *  is squashed every iteration in which the branch is taken, and
   *  executes once at the loop exit (when the branch falls through).
   *  With delay slot ON, the slot executes on every iteration including
   *  the loop exit, so r3 ends up taken-count + 1 times larger. */
  @Test(timeout=5000)
  public void testDelaySlotNotTakenLoop() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-not-taken-loop.s");
    // Loop body always runs 3 times in both modes (r1 starts at 3).
    collector.checkThat("r2 (loop body counter) without delay slot",
        statuses.get(false).gprValues[2], equalTo(3L));
    collector.checkThat("r2 (loop body counter) with delay slot",
        statuses.get(true).gprValues[2], equalTo(3L));
    // r3: with delay slot OFF, slot only runs at loop exit → +7.
    collector.checkThat("r3 (slot increment) without delay slot",
        statuses.get(false).gprValues[3], equalTo(7L));
    // r3: with delay slot ON, slot runs once per BNEZ (3 iterations: 2
    // taken + 1 not taken) → 3 × 7 = 21.
    collector.checkThat("r3 (slot increment) with delay slot",
        statuses.get(true).gprValues[3], equalTo(21L));
  }

  /** SYSCALL 0 (terminating) in the delay slot. The slot's side
   *  effect — terminate — must take effect; the branch target must not
   *  execute. Behavior diverges depending on whether the slot is
   *  squashed: with delay slot OFF the SYSCALL is squashed, the program
   *  falls through to the branch target which contains a BREAK, so a
   *  BreakException is observed; with delay slot ON the SYSCALL fires
   *  and the program terminates cleanly. */
  @Test(timeout=5000)
  public void testDelaySlotSyscallInSlotEnabled() throws Exception {
    CpuTestStatus s = runMipsTestWithDelaySlot("delay-slot-syscall-in-slot.s");
    collector.checkThat("r2 must be 0 (target not reached)",
        s.gprValues[2], equalTo(0L));
  }

  @Test(expected = BreakException.class, timeout=5000)
  public void testDelaySlotSyscallInSlotDisabled() throws Exception {
    runMipsTestWithoutDelaySlot("delay-slot-syscall-in-slot.s");
  }

  /** Non-terminating SYSCALL (here SYSCALL 5, printf) in the delay slot.
   *  Unlike SYSCALL 0 / HALT, this kind of syscall has no termination
   *  side-effect, so the slot must run to completion and execution must
   *  continue at the branch target. With delay slot OFF the SYSCALL is
   *  squashed and the branch target executes anyway; the difference is
   *  in R1, which SYSCALL 5 sets to the number of bytes "printed" (2 for
   *  "ok") only when the slot actually runs. */
  @Test(timeout=5000)
  public void testDelaySlotSyscallNontermInSlotEnabled() throws Exception {
    CpuTestStatus s = runMipsTestWithDelaySlot("delay-slot-syscall-nonterm-in-slot.s");
    collector.checkThat("target executed after non-terminating SYSCALL ran in the slot",
        s.gprValues[2], equalTo(42L));
    collector.checkThat("R1 holds the SYSCALL 5 return value (length of \"ok\")",
        s.gprValues[1], equalTo(2L));
  }

  @Test(timeout=5000)
  public void testDelaySlotSyscallNontermInSlotDisabled() throws Exception {
    CpuTestStatus s = runMipsTestWithoutDelaySlot("delay-slot-syscall-nonterm-in-slot.s");
    collector.checkThat("target executed after non-terminating SYSCALL was squashed",
        s.gprValues[2], equalTo(42L));
    collector.checkThat("R1 untouched (SYSCALL was squashed in the slot)",
        s.gprValues[1], equalTo(1L));
  }

  /** HALT in the delay slot. Same semantics as SYSCALL 0 in the slot. */
  @Test(timeout=5000)
  public void testDelaySlotHaltInSlotEnabled() throws Exception {
    CpuTestStatus s = runMipsTestWithDelaySlot("delay-slot-halt-in-slot.s");
    collector.checkThat("r2 must be 0 (target not reached)",
        s.gprValues[2], equalTo(0L));
  }

  @Test(expected = BreakException.class, timeout=5000)
  public void testDelaySlotHaltInSlotDisabled() throws Exception {
    runMipsTestWithoutDelaySlot("delay-slot-halt-in-slot.s");
  }

  /** BREAK in the delay slot. With delay slot OFF the BREAK is silently
   *  squashed (existing behavior); with delay slot ON the BREAK is
   *  propagated as a BreakException (handled in CPU.java's JumpException
   *  handler). */
  @Test(timeout=5000)
  public void testDelaySlotBreakInSlotDisabled() throws Exception {
    CpuTestStatus s = runMipsTestWithoutDelaySlot("delay-slot-break-in-slot.s");
    collector.checkThat("target executed after BREAK was squashed",
        s.gprValues[2], equalTo(7L));
  }

  @Test(expected = BreakException.class, timeout=5000)
  public void testDelaySlotBreakInSlotEnabled() throws Exception {
    runMipsTestWithDelaySlot("delay-slot-break-in-slot.s");
  }

  /** Synchronous arithmetic overflow exception raised by the EX stage of
   *  the delay slot instruction. With delay slot OFF the offending
   *  instruction is squashed and the program terminates cleanly via
   *  the SYSCALL in the branch target; with delay slot ON the overflow
   *  fires and propagates as a SynchronousException. */
  @Test(expected = SynchronousException.class, timeout=5000)
  public void testDelaySlotOverflowInSlotEnabled() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTestWithDelaySlot("delay-slot-overflow-in-slot.s");
  }

  @Test(timeout=5000)
  public void testDelaySlotOverflowInSlotDisabled() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTestWithoutDelaySlot("delay-slot-overflow-in-slot.s");
  }

  /** Divide-by-zero in the delay slot. With delay slot OFF the DDIV is
   *  squashed and the program terminates cleanly via the syscall in
   *  the branch target; with delay slot ON the DDIV runs and raises a
   *  DivisionByZeroException (a SynchronousException). */
  @Test(timeout=5000)
  public void testDelaySlotDiv0InSlotDisabled() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTestWithoutDelaySlot("delay-slot-div0-in-slot.s");
  }

  @Test(expected = SynchronousException.class, timeout=5000)
  public void testDelaySlotDiv0InSlotEnabled() throws Exception {
    config.putBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    runMipsTestWithDelaySlot("delay-slot-div0-in-slot.s");
  }

  /** Sanity test: a NOP in the delay slot is the canonical, well-defined
   *  pattern; observable register state must be identical with and
   *  without delay slot support. */
  @Test(timeout=5000)
  public void testDelaySlotBubbleSlot() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-bubble-slot.s");
    collector.checkThat("r2 without delay slot",
        statuses.get(false).gprValues[2], equalTo(11L));
    collector.checkThat("r2 with delay slot",
        statuses.get(true).gprValues[2], equalTo(11L));
  }

  /** JAL link register. EduMIPS64 stores PC-4 at JAL.ID() time; at that
   *  point the slot has already been fetched so PC = JAL_PC + 8 and the
   *  stored value is JAL_PC + 4 = address of the slot. This is
   *  architecturally correct for the case where the slot is squashed
   *  (delay slot OFF: the "return point" is the first instruction after
   *  the JAL, i.e. the slot's address). When the delay slot is enabled
   *  the MIPS spec requires R31 = JAL_PC + 8 (after-slot); this test
   *  pins down the value EduMIPS64 produces so a future fix that
   *  changes the semantics has to update the assertion explicitly. */
  @Test(timeout=5000)
  public void testDelaySlotJalLink() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-jal-link.s");
    // JAL is at PC = 0; slot at PC = 4; after-slot at PC = 8.
    collector.checkThat("JAL link value without delay slot (slot address)",
        statuses.get(false).gprValues[5], equalTo(4L));
    collector.checkThat("JAL link value with delay slot (after slot)",
        statuses.get(true).gprValues[5], equalTo(8L));
  }

  /** JALR link register, same semantics as JAL. */
  @Test(timeout=5000)
  public void testDelaySlotJalrLink() throws Exception {
    Map<Boolean, CpuTestStatus> statuses = runMipsTestWithAndWithoutDelaySlot("delay-slot-jalr-link.s");
    // JALR is at PC = 8; slot at PC = 12; after-slot at PC = 16.
    collector.checkThat("JALR link value without delay slot (slot address)",
        statuses.get(false).gprValues[5], equalTo(12L));
    collector.checkThat("JALR link value with delay slot (after slot)",
        statuses.get(true).gprValues[5], equalTo(16L));
  }
}
