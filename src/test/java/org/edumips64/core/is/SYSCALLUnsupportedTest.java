package org.edumips64.core.is;

import org.edumips64.BaseTest;
import org.edumips64.client.WebInputReader;
import org.edumips64.core.CPU;
import org.edumips64.core.CacheSimulator;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.utils.io.NullFileUtils;
import org.edumips64.utils.io.StringWriter;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests that SYSCALLs that cannot be served by the current environment
 * (e.g. file I/O SYSCALLs issued from the Web UI, which is backed by
 * {@link NullFileUtils}) are surfaced to the user as a clear error and
 * interrupt execution, rather than being silently swallowed.
 */
public class SYSCALLUnsupportedTest extends BaseTest {
  private Memory memory;
  private CPU cpu;
  private IOManager ioManager;
  private InstructionBuilder builder;

  @Before
  public void setUp() {
    memory = new Memory();
    CacheSimulator cacheSimulator = new CacheSimulator();
    cpu = new CPU(memory, config, new BUBBLE());
    ioManager = new IOManager(new NullFileUtils(), memory);
    ioManager.setStdInput(new WebInputReader());
    ioManager.setStdOutput(new StringWriter());
    builder = new InstructionBuilder(memory, ioManager, cpu, cacheSimulator, config);
  }

  private Instruction buildSyscall(int number) {
    Instruction instruction = builder.buildInstruction("SYSCALL");
    instruction.setParams(List.of(number));
    return instruction;
  }

  /** Runs the SYSCALL pipeline stages up to and including MEM, where the
   *  environment checks live. */
  private void runThroughMem(Instruction instruction) throws Exception {
    instruction.IF();
    instruction.ID();
    instruction.EX();
    instruction.MEM();
  }

  @Test
  public void syscall1OpenThrowsOnNullFileSystem() throws Exception {
    cpu.getRegister(14).writeDoubleWord(0);
    Instruction instruction = buildSyscall(1);
    UnsupportedSyscallException e =
        assertThrows(UnsupportedSyscallException.class, () -> runThroughMem(instruction));
    assertTrue("error message should mention SYSCALL 1",
        e.getMessage().contains("SYSCALL 1"));
    assertEquals("SYSCALL 1", e.getInstructionName());
    assertEquals("MEM", e.getStage());
    assertEquals(org.edumips64.core.SynchronousExceptionCode.UNSUPPORTED_SYSCALL, e.getCode());
  }

  @Test
  public void syscall2CloseThrowsOnNullFileSystem() throws Exception {
    MemoryElement fdCell = memory.getCellByAddress(0);
    fdCell.writeDoubleWord(42);
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = buildSyscall(2);
    UnsupportedSyscallException e =
        assertThrows(UnsupportedSyscallException.class, () -> runThroughMem(instruction));
    assertTrue("error message should mention SYSCALL 2",
        e.getMessage().contains("SYSCALL 2"));
    assertEquals("SYSCALL 2", e.getInstructionName());
    assertEquals("MEM", e.getStage());
  }

  @Test
  public void syscall3ReadThrowsForNonStdinFdOnNullFileSystem() throws Exception {
    // fd (non-stdin), buf addr, count — laid out at address 0, 8, 16.
    MemoryElement fd = memory.getCellByAddress(0);
    fd.writeDoubleWord(7);
    MemoryElement bufAddr = memory.getCellByAddress(8);
    bufAddr.writeDoubleWord(24);
    MemoryElement count = memory.getCellByAddress(16);
    count.writeDoubleWord(3);
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = buildSyscall(3);
    UnsupportedSyscallException e =
        assertThrows(UnsupportedSyscallException.class, () -> runThroughMem(instruction));
    assertTrue("error message should mention SYSCALL 3",
        e.getMessage().contains("SYSCALL 3"));
    assertTrue("error message should mention the offending fd",
        e.getMessage().contains("7"));
    assertEquals("SYSCALL 3", e.getInstructionName());
    assertEquals("MEM", e.getStage());
  }

  @Test
  public void syscall4WriteThrowsForNonStdoutFdOnNullFileSystem() throws Exception {
    MemoryElement fd = memory.getCellByAddress(0);
    fd.writeDoubleWord(7);
    MemoryElement bufAddr = memory.getCellByAddress(8);
    bufAddr.writeDoubleWord(24);
    MemoryElement count = memory.getCellByAddress(16);
    count.writeDoubleWord(3);
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = buildSyscall(4);
    UnsupportedSyscallException e =
        assertThrows(UnsupportedSyscallException.class, () -> runThroughMem(instruction));
    assertTrue("error message should mention SYSCALL 4",
        e.getMessage().contains("SYSCALL 4"));
    assertTrue("error message should mention the offending fd",
        e.getMessage().contains("7"));
    assertEquals("SYSCALL 4", e.getInstructionName());
    assertEquals("MEM", e.getStage());
  }

  @Test
  public void syscall4WriteToStdoutDoesNotThrowOnNullFileSystem() throws Exception {
    // Writes to stdout (fd 1) must continue to work even without a filesystem.
    MemoryElement fd = memory.getCellByAddress(0);
    fd.writeDoubleWord(IOManager.STDOUT_FD);
    MemoryElement bufAddr = memory.getCellByAddress(8);
    bufAddr.writeDoubleWord(24);
    MemoryElement count = memory.getCellByAddress(16);
    count.writeDoubleWord(0); // write zero bytes: avoids needing data at buf.
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = buildSyscall(4);
    // Must not throw UnsupportedSyscallException.
    runThroughMem(instruction);
  }

  @Test
  public void invalidSyscallNumberThrows() throws Exception {
    Instruction instruction = buildSyscall(42);
    UnsupportedSyscallException e =
        assertThrows(UnsupportedSyscallException.class, () -> {
          instruction.IF();
          instruction.ID();
        });
    assertTrue("error message should mention the invalid number",
        e.getMessage().contains("42"));
    assertEquals("SYSCALL 42", e.getInstructionName());
    assertEquals("ID", e.getStage());
  }
}

