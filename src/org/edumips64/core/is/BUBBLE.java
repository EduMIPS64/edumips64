/*
 * BUBBLE.java
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

package org.edumips64.core.is;
import org.edumips64.core.*;
import org.edumips64.utils.*;

/**Name:       BUBBLE
 * Purpose:    Creating null spaces in the pipeline
 * Type:       SPECIAL
 * Format:     BUBBLE
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 */
public class BUBBLE extends Instruction {

  /** Creates a new instance of BUBBLE */
  public BUBBLE() {
    name = " ";
    fullname = " ";
  }

  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException {

  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
  }

  public void WB() throws IrregularStringOfBitsException {
  }

  public void pack() throws IrregularStringOfBitsException {
  }

}
