/* Pipeline.java
 *
 * Javacsript-friendly representation of the CPU integer pipeline.
 * 
 * (c) 2020 Andrea Spadaccini
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
package org.edumips64.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace=JsPackage.GLOBAL, name="Object", isNative=true)
public class Pipeline {
    public Instruction IF;
    public Instruction ID;
    public Instruction EX;
    public Instruction MEM;
    public Instruction WB;

    // FPU stages.
    public Instruction FPDivider;
    public Instruction FPAdder1;
    public Instruction FPAdder2;
    public Instruction FPAdder3;
    public Instruction FPAdder4;
    public Instruction FPMultiplier1;
    public Instruction FPMultiplier2;
    public Instruction FPMultiplier3;
    public Instruction FPMultiplier4;
    public Instruction FPMultiplier5;
    public Instruction FPMultiplier6;
    public Instruction FPMultiplier7;
}