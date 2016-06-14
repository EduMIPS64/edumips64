/* CpuTests.java
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
import org.edumips64.core.is.*;
import org.edumips64.ui.CycleBuilder;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.io.LocalFileUtils;
import org.edumips64.utils.io.LocalWriter;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Scanner;

import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.hamcrest.CoreMatchers.*;

@RunWith(JUnit4.class)
public class CpuTests {
  private CPU cpu;
  private LocalFileUtils lfu;
  private Parser parser;
  private static String testsLocation = "test/org/edumips64/data/";
  private final static Logger log = Logger.getLogger(CpuTestStatus.class.getName());
  private Dinero dinero = Dinero.getInstance();
  private ConfigStore config = ConfigManager.getTmpConfig();

  @Rule
  public ErrorCollector collector = new ErrorCollector();

  /** Class that holds the parts of the CPU status that need to be tested
   * after the execution of a test case.
   */
  class CpuTestStatus {
    int cycles;
    int instructions;
    int rawStalls, wawStalls, memStalls;
    String traceFile;

    CpuTestStatus(CPU cpu, String dineroTrace) {
      cycles = cpu.getCycles();
      instructions = cpu.getInstructions();
      wawStalls = cpu.getWAWStalls();
      rawStalls = cpu.getRAWStalls();
      memStalls = cpu.getStructuralStallsMemory();
      traceFile = dineroTrace;

      log.warning("Got " + cycles + " cycles, " + instructions + " instructions, " + rawStalls + " RAW Stalls and " + wawStalls + " WAW stalls.");
    }
  }

  /** Class to hold the FPU exceptions configuration.
   */
  class FPUExceptionsConfig {
    boolean invalidOperation, overflow, underflow, divideByZero;

    // Constructor, initializes the values from the Config store.
    public FPUExceptionsConfig() {
      invalidOperation = config.getBoolean("INVALID_OPERATION");
      overflow = config.getBoolean("OVERFLOW");
      underflow = config.getBoolean("UNDERFLOW");
      divideByZero = config.getBoolean("DIVIDE_BY_ZERO");
    }

    // Restore values to the config Store.
    void restore() {
      config.putBoolean("INVALID_OPERATION", invalidOperation);
      config.putBoolean("OVERFLOW", overflow);
      config.putBoolean("UNDERFLOW", underflow);
      config.putBoolean("DIVIDE_BY_ZERO", divideByZero);
    }
  }

  private FPUExceptionsConfig fec;

  @BeforeClass
  public static void setup() {
    // Disable logs of level lesser than WARNING.
    Logger rootLogger = log.getParent();

    for (Handler h : rootLogger.getHandlers()) {
      h.setLevel(java.util.logging.Level.SEVERE);
    }
  }

  @Before
  public void testSetup() {
    cpu = CPU.getInstance();
    cpu.setStatus(CPU.CPUStatus.READY);
    lfu = new LocalFileUtils();
    IOManager.createInstance(lfu);
    Parser.createInstance(lfu);
    parser = Parser.getInstance();
    Instruction.setEnableForwarding(true);
    fec = new FPUExceptionsConfig();
    ConfigManager.setConfig(config);
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
    log.warning("================================= Starting test " + testPath);
    cpu.reset();
    testPath = testsLocation + testPath;
    CycleBuilder builder = new CycleBuilder();

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

      cpu.setStatus(CPU.CPUStatus.RUNNING);

      while (true) {
        cpu.step();
        builder.step();
      }
    } catch (HaltException e) {
      log.warning("================================= Finished test " + testPath);

      File tmp = File.createTempFile("edumips64", "xdin");
      tmp.deleteOnExit();
      LocalWriter w = new LocalWriter(tmp.getAbsolutePath(), false);
      dinero.writeTraceData(w);
      w.close();

      return new CpuTestStatus(cpu, tmp.getAbsolutePath());
    } finally {
      cpu.reset();
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
    boolean forwardingStatus = Instruction.getEnableForwarding();
    Map<ForwardingStatus, CpuTestStatus> statuses = new HashMap<>();

    Instruction.setEnableForwarding(true);
    statuses.put(ForwardingStatus.ENABLED, runMipsTest(testPath));

    Instruction.setEnableForwarding(false);
    statuses.put(ForwardingStatus.DISABLED, runMipsTest(testPath));

    Instruction.setEnableForwarding(forwardingStatus);
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
    CpuTestStatus s = runMipsTest(path);
    String goldenTrace = testsLocation + path + ".xdin.golden";

    String golden = new Scanner(new File(goldenTrace)).useDelimiter("\\A").next();
    String trace = new Scanner(new File(s.traceFile)).useDelimiter("\\A").next();
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
      collector.checkThat(status.cycles, equalTo(6));
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
  public void testReadFile() throws Exception {
    runMipsTest("syscall-read.s");
  }

  @Test
  public void testReadWriteFile() throws Exception {
    // TODO(andrea): clean up the test file.
    runMipsTest("read-write.s");
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
    // Simple test.
    runForwardingTest("forwarding.s", 16, 19, 10);

    // Tests taken from Hennessy & Patterson, Appendix A
    runForwardingTest("forwarding-hp-pA16.s", 11, 13, 6);
    runForwardingTest("forwarding-hp-pA18.s", 9, 13, 4);
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
    collector.checkThat(filename + ": cycles with forwarding.", statuses.get(ForwardingStatus.ENABLED).cycles, equalTo(20));
    collector.checkThat(filename + ": instructions with forwarding.", statuses.get(ForwardingStatus.ENABLED).instructions, equalTo(5));
    collector.checkThat(filename + ": WAW stalls with forwarding." ,statuses.get(ForwardingStatus.ENABLED).wawStalls, equalTo(7));
    collector.checkThat(filename + ": RAW stalls with forwarding.", statuses.get(ForwardingStatus.ENABLED).rawStalls, equalTo(1));

    // Without forwarding
    collector.checkThat(filename + ": cycles without forwarding.", statuses.get(ForwardingStatus.DISABLED).cycles, equalTo(21));
    collector.checkThat(filename + ": instructions without forwarding.", statuses.get(ForwardingStatus.DISABLED).instructions, equalTo(5));
    collector.checkThat(filename + ": WAW stalls without forwarding." ,statuses.get(ForwardingStatus.DISABLED).wawStalls, equalTo(7));
    collector.checkThat(filename + ": RAW stalls without forwarding.", statuses.get(ForwardingStatus.DISABLED).rawStalls, equalTo(2));
  }

  @Test
  public void testFPUMul() throws Exception {
    // This test contains code that raises exceptions, let's disable them.
    config.putBoolean("INVALID_OPERATION", false);
    config.putBoolean("OVERFLOW", false);
    config.putBoolean("UNDERFLOW", false);
    config.putBoolean("DIVIDE_BY_ZERO", false);
    Map<ForwardingStatus, CpuTestStatus> statuses = runMipsTestWithAndWithoutForwarding("fpu-mul.s");

    // Same behaviour with and without forwarding.
    int expected_cycles = 43, expected_instructions = 32, expected_mem_stalls = 6;
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).cycles, equalTo(expected_cycles));
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).instructions, equalTo(expected_instructions));
    collector.checkThat(statuses.get(ForwardingStatus.ENABLED).memStalls, equalTo(expected_mem_stalls));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).cycles, equalTo(expected_cycles));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).instructions, equalTo(expected_instructions));
    collector.checkThat(statuses.get(ForwardingStatus.DISABLED).memStalls, equalTo(expected_mem_stalls));
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
    runForwardingTest("issue51-halt.s", 12, 18, 6);
    runForwardingTest("issue51-syscall0.s", 12, 18, 6);
  }

  /* Issue #68: JR does not respect RAW stalls. */
  @Test
  public void testRAWForRTypeFlowControl() throws Exception {
    runMipsTestWithAndWithoutForwarding("jr-raw.s");
    runMipsTestWithAndWithoutForwarding("jalr-raw.s");
  }
}
