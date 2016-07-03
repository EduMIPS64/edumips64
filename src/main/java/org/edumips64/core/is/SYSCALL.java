/*
 * SYSCALL.java
 *
 * (c) 2006 EduMips64 project - Andrea Spadaccini
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

package org.edumips64.core.is;

import org.edumips64.core.*;
import org.edumips64.utils.*;
import org.edumips64.utils.io.*;
import java.util.logging.Logger;

/** SYSCALL instruction, used to issue system calls.
 *
 * @author Andrea Spadaccini
 */
public class SYSCALL extends Instruction {
  private static final Logger logger = Logger.getLogger(SYSCALL.class.getName());

  String OPCODE_VALUE = "000000";
  String FINAL_VALUE = "001100";
  private int syscall_n;
  private int return_value;
  private long address;

  private Dinero din;
  private IOManager iom;
  private Memory memory;

  SYSCALL(Memory memory, IOManager iom) {
    this.syntax = "%U";
    this.paramCount = 1;
    this.name = "SYSCALL";
    this.iom = iom;
    this.memory = memory;
  }

  public void IF() {
    syscall_n = params.get(0);
    logger.info("SYSCALL " + syscall_n + " (" + this.hashCode() + ") is in IF");

    try {
      dinero.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
    }
  }

  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
    if (syscall_n == 0) {
      logger.info("Stopping CPU due to SYSCALL (" + this.hashCode() + ")");
      cpu.setStatus(CPU.CPUStatus.STOPPING);
    } else if ((syscall_n > 0) && (syscall_n <= 5)) {
      Register r14 = cpu.getRegister(14);

      if (r14.getWriteSemaphore() > 0) {
        throw new RAWException();
      }

      Register r1 = cpu.getRegister(1);

      // In WB, R1 <- Return value
      r1.incrWriteSemaphore();
      address = r14.getValue();
      logger.info("SYSCALL (" + this.hashCode() + "): locked register R14. Value = " + address);
    } else {
      // TODO: invalid syscall
      logger.info("INVALID SYSCALL (" + this.hashCode() + ")");
    }
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
    logger.info("SYSCALL (" + this.hashCode() + ") -> EX");
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
    logger.info("SYSCALL (" + this.hashCode() + ") -> MEM");

    if (syscall_n == 1) {
      // int open(const char* filename, int flags)
      String filename = fetchString(address);
      int flags_address = (int) address + filename.length();
      flags_address += 8 - (flags_address % 8);

      MemoryElement flags_m = memory.getCellByAddress(flags_address);
      int flags = (int) flags_m.getValue();

      // Memory access for the string and the flags (note the <=)
      for (int i = (int) address; i <= flags_address; i += 8) {
        dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
      }

      logger.info("We must open " + filename + " with flags " + flags);

      return_value = -1;

      try {
        return_value = iom.open(filename, flags);
      } catch (Exception e) {
        logger.info("Error in executing the open(), the syscall will fail.");
        logger.info(e.toString());
      }

    } else if (syscall_n == 2) {
      // int close(int fd)
      MemoryElement fd_cell = memory.getCellByAddress(address);
      int fd = (int) fd_cell.getValue();
      logger.info("Closing fd " + fd);
      return_value = iom.close(fd);
    } else if ((syscall_n == 3) || (syscall_n == 4)) {
      // int read(int fd, void* buf, int count)
      // int write(int fd, void* buf, int count)
      int fd, count;
      long buf_addr;

      MemoryElement temp = memory.getCellByAddress(address);
      fd = (int) temp.getValue();
      address += 8;

      temp = memory.getCellByAddress(address);
      buf_addr = temp.getValue();
      address += 8;

      temp = memory.getCellByAddress(address);
      count = (int) temp.getValue();
      address += 8;

      return_value = -1;

      try {
        if (syscall_n == 3) {
          logger.info("SYSCALL (" + this.hashCode() + "): trying to read from fd " + fd + " " + count + " bytes, writing them to address " + buf_addr);
          return_value = iom.read(fd, buf_addr, count);
        } else {
          logger.info("SYSCALL (" + this.hashCode() + "): trying to write to fd " + fd + " " + count + " bytes, reading them from address " + buf_addr);
          return_value = iom.write(fd, buf_addr, count);
        }
      } catch (Exception e) {
        logger.info("Error in executing the read(), the syscall will fail.");
        logger.info(e.toString());
      }
    } else if (syscall_n == 5) {
      StringBuilder temp = new StringBuilder();

      // In the address variable (content of R14) we have the address of
      // the format string, that we get and put in the format_string_address variable
      logger.info("Reading memory cell at address " + address + ", searching for the address of the format string");
      MemoryElement tempMemCell = memory.getCellByAddress(address);
      int format_string_address = (int) tempMemCell.getValue();

      // Recording in the tracefile the last memory access
      dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, address)), 8);

      // Fetching the format string
      String format_string = fetchString(format_string_address);
      logger.info("Read " + format_string);

      // Going to the next memory cell to start fetching parameters.
      int next_param_address = (int) address + 8;

      // Let's record in the tracefile the format string's memory access
      // t1 will hold the address of the last memory cell accessed
      // while we were reading format_string.
      int t1 = format_string_address + format_string.length();
      t1 += 8 - (t1 % 8);

      for (int i = format_string_address; i < t1; i += 8) {
        dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
      }

      int oldIndex = 0;
      int newIndex = 0;

      while ((newIndex = format_string.indexOf('%', oldIndex)) >= 0) {
        char type = format_string.charAt(newIndex + 1);
        logger.info("Found a placeholder... type " + type);
        temp.append(format_string.substring(oldIndex, newIndex));

        switch (type) {
        case 's':   // %s
          tempMemCell = memory.getCellByAddress(next_param_address);
          int str_address = (int) tempMemCell.getValue();
          logger.info("Retrieving the string @ " + str_address + "...");
          String param = fetchString(str_address);

          next_param_address += 8;
          /* Old, buggy behavior
          int old_param_address = next_param_address;
          next_param_address += param.length();
          next_param_address += 8 - (next_param_address % 8);
          */

          // Tracefile entry for this string
          int t2 = str_address + param.length();
          t2 += 8 - (t2 % 8);

          for (int i = str_address; i < t2; i += 8) {
            dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
          }

          logger.info("Got " + param);
          temp.append(param);
          break;
        case 'i':   // %i
        case 'd':   // %d
          logger.info("Retrieving the integer @ " + next_param_address + "...");
          MemoryElement memCell = memory.getCellByAddress(next_param_address);

          // Tracefile entry for this memory access
          dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, next_param_address)), 8);

          Long val = memCell.getValue();
          next_param_address += 8;
          temp.append(val.toString());
          logger.info("Got " + val);
          break;
        case '%':   // %%
          logger.info("Literal %...");
          temp.append('%');
          break;
        default:
          logger.info("Unknown placeholder");
          break;
        }

        oldIndex = newIndex + 2;
      }

      temp.append(format_string.substring(oldIndex));
      logger.info("That became " + temp.toString());

      //This prints to StdOutput.
      try {
        iom.write(1, temp.toString());
      } catch (WriteException e) {
        logger.info("Error in executing the printf(), the syscall will fail.");
        logger.info(e.toString());
      }

      return_value = temp.length();
    }
  }

  private String fetchString(long address) throws MemoryElementNotFoundException {
    StringBuilder temp = new StringBuilder();
    boolean end_of_string = false;

    while (!end_of_string) {
      MemoryElement memEl = memory.getCellByAddress(address);

      for (int i = 0; i < 8; ++i) {
        int tempInt = memEl.readByte(i);

        if (tempInt == 0) {
          end_of_string = true;
          break;
        }

        char c = (char)(tempInt);
        temp.append(c);
      }

      address += 8;
    }

    return temp.toString();
  }

  public void WB() throws IrregularStringOfBitsException, HaltException {
    logger.info("SYSCALL (" + this.hashCode() + ") -> WB. n = " + syscall_n);

    if (syscall_n == 0) {
      logger.info("Stopped CPU due to SYSCALL (" + this.hashCode() + ")");
      cpu.setStatus(CPU.CPUStatus.HALTED);
      throw new HaltException();
    } else if (syscall_n > 0 && syscall_n <= 5) {
      logger.info("SYSCALL (" + this.hashCode() + "): setting R1 to " + return_value);
      Register r1 = cpu.getRegister(1);
      logger.info("SYSCALL (" + this.hashCode() + "): got R1");
      r1.setBits(Converter.intToBin(64, return_value), 0);
      logger.info("SYSCALL (" + this.hashCode() + "): set R1 to " + return_value);
      r1.decrWriteSemaphore();
      logger.info("SYSCALL (" + this.hashCode() + "): decremented write semaphore");
    }

    logger.info("SYSCALL (" + this.hashCode() + ") exiting from WB. n = " + syscall_n);
  }

  public void pack() throws IrregularStringOfBitsException {
    /* First 6 bits -> 000000 (SPECIAL) */
    repr.setBits(OPCODE_VALUE, 0);
    /* Next 20 bits -> binary value of the immediate parameter. */
    repr.setBits(Converter.intToBin(20, params.get(0)), 6);
    /* Last 6 bits -> 001100 (SYSCALL) */
    repr.setBits(FINAL_VALUE, 26);
  }
}
