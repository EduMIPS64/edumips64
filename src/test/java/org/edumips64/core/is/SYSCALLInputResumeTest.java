package org.edumips64.core.is;

import org.edumips64.BaseTest;
import org.edumips64.client.WebInputReader;
import org.edumips64.core.CPU;
import org.edumips64.core.CacheSimulator;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.utils.io.InputNeededException;
import org.edumips64.utils.io.NullFileUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SYSCALLInputResumeTest extends BaseTest {
  @Test
  public void syscall3ResumeWritesToTheOriginalBufferAddress() throws Exception {
    Memory memory = new Memory();
    CacheSimulator cacheSimulator = new CacheSimulator();
    CPU cpu = new CPU(memory, config, new BUBBLE());
    WebInputReader stdin = new WebInputReader();
    IOManager ioManager = new IOManager(new NullFileUtils(), memory);
    ioManager.setStdInput(stdin);

    InstructionBuilder builder = new InstructionBuilder(memory, ioManager, cpu, cacheSimulator, config);
    Instruction instruction = builder.buildInstruction("SYSCALL");
    instruction.setParams(List.of(3));

    MemoryElement fd = memory.getCellByAddress(0);
    fd.writeDoubleWord(0);

    MemoryElement bufAddr = memory.getCellByAddress(8);
    bufAddr.writeDoubleWord(24);

    MemoryElement count = memory.getCellByAddress(16);
    count.writeDoubleWord(3);

    cpu.getRegister(14).writeDoubleWord(0);

    instruction.IF();
    instruction.ID();

    try {
      instruction.MEM();
    } catch (InputNeededException expected) {
      // Expected on the first pass: the UI must provide stdin.
    }

    stdin.setNextInput("123");
    instruction.MEM();

    MemoryElement readBuffer = memory.getCellByAddress(24);
    assertEquals('1', readBuffer.readByte(0));
    assertEquals('2', readBuffer.readByte(1));
    assertEquals('3', readBuffer.readByte(2));
  }
}
