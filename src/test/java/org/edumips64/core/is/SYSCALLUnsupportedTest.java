package org.edumips64.core.is;

import org.edumips64.BaseTest;
import org.edumips64.core.CPU;
import org.edumips64.core.CacheSimulator;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.core.Register;
import org.edumips64.utils.io.NullFileUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests that file I/O SYSCALLs fail gracefully when the environment does
 * not support file I/O (e.g. the Web UI, which uses {@link NullFileUtils}).
 * The expectation is that R1 is set to -1 and no unhandled exception is
 * thrown out of the SYSCALL execution pipeline.
 */
public class SYSCALLUnsupportedTest extends BaseTest {
  private Memory memory;
  private CPU cpu;
  private InstructionBuilder builder;

  @Before
  public void setUp() {
    memory = new Memory();
    CacheSimulator cacheSimulator = new CacheSimulator();
    cpu = new CPU(memory, config, new BUBBLE());
    IOManager ioManager = new IOManager(new NullFileUtils(), memory);
    builder = new InstructionBuilder(memory, ioManager, cpu, cacheSimulator, config);
  }

  private void runSyscall(Instruction instruction) throws Exception {
    instruction.IF();
    instruction.ID();
    instruction.EX();
    instruction.MEM();
    instruction.WB();
  }

  private long signExtendedR1() {
    // R1 is written as a 64-bit two's complement value; interpret it as long.
    Register r1 = cpu.getRegister(1);
    return r1.getValue();
  }

  @Test
  public void syscall1OpenFailsGracefullyWithNullFileUtils() throws Exception {
    // Write a dummy filename ("a\0") at address 0, and flags at address 8.
    MemoryElement filenameCell = memory.getCellByAddress(0);
    filenameCell.writeByte((int) 'a', 0);
    MemoryElement flagsCell = memory.getCellByAddress(8);
    flagsCell.writeDoubleWord(0x01); // O_RDONLY

    // R14 holds the address of the filename.
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = builder.buildInstruction("SYSCALL");
    instruction.setParams(List.of(1));

    runSyscall(instruction);

    // When file I/O is unsupported, open() must fail with -1 and must not
    // throw an unhandled exception.
    assertEquals("open() should return -1 when file I/O is unsupported",
        -1L, signExtendedR1());
  }

  @Test
  public void syscall2CloseFailsGracefullyWithInvalidFd() throws Exception {
    // R14 points to a memory cell holding an fd that was never opened.
    MemoryElement fdCell = memory.getCellByAddress(0);
    fdCell.writeDoubleWord(42);
    cpu.getRegister(14).writeDoubleWord(0);

    Instruction instruction = builder.buildInstruction("SYSCALL");
    instruction.setParams(List.of(2));

    // Must not throw an unhandled exception.
    runSyscall(instruction);

    // close() on an unknown fd should return -1.
    assertEquals("close() on an unknown fd should return -1",
        -1L, signExtendedR1());
  }
}
