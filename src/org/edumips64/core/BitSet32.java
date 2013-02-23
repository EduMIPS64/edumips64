/* BitSet32.java
 *
 * This class models a 32-bit array, useful for instruction representation.
 * (c) 2006 Salvatore Scellato
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
package org.edumips64.core;

import org.edumips64.utils.*;
import java.util.BitSet;

/** This class models a 32-bit array, useful for instruction representation.
 * @author Salvatore Scellato
 * */

public class BitSet32 extends FixedBitSet {


  /** Creates a default new instance of BitSet32 */
  public BitSet32() {
    super();
    size = 32;
  }

  public static void main(String[] args) throws Exception {

    BitSet32 bs = new BitSet32();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < 32; i++) {
      buf.append("1");
    }

    BitSet32 bs2 = new BitSet32();
    bs2.setBits(new String(buf), 0);
    bs.setBits("011100110101101100101011001101010", 0);
    bs.reset(true);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Binary string: " + bs2.getBinString());
    System.out.println("Hex string: " + bs2.getHexString());
    System.out.println(Converter.binToHex("11111"));
  }
}



