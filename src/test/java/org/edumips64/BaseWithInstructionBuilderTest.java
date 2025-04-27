/* BaseWithInstructionBuilderTest.java
 *
 * Base class for tests that need to create instructions.
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
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.LocalFileUtils;
import org.edumips64.utils.io.StringWriter;

import org.junit.Before;

public class BaseWithInstructionBuilderTest extends BaseTest {
  protected CPU cpu;
  protected SymbolTable symTab;
  protected CacheSimulator cachesim;
  protected StringWriter stdOut;
  protected Memory memory;
  protected InstructionBuilder instructionBuilder;
  protected FileUtils lfu;
  
  @Before
  public void testSetup() {
    memory = new Memory();
    cpu = new CPU(memory, config, new BUBBLE());
    cpu.setStatus(CPU.CPUStatus.READY);
    cachesim = new CacheSimulator();
    symTab = new SymbolTable(memory);
    stdOut = new StringWriter();
    lfu = new LocalFileUtils();
    IOManager iom = new IOManager(lfu, memory);
    iom.setStdOutput(stdOut);
    instructionBuilder = new InstructionBuilder(memory, iom, cpu, cachesim, config);
  }
}
