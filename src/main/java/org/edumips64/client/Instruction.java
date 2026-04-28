/* Instruction.java
 *
 * Javascript-friendly wrapper for InstructionInterface.
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

import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

// Wrapper for Instruction / InstructionInterface.
@JsType
public class Instruction {
    public String Name;
    public String Code;
    public String Comment;
    public int SerialNumber;
    public int Address;
    public int Line;
    public String BinaryRepresentation;
    public String OpCode;
    /**
     * Last state recorded for this instruction by {@code CycleBuilder} (one of
     * {@code "IF"}, {@code "ID"}, {@code "EX"}, {@code "MEM"}, {@code "WB"},
     * {@code "RAW"}, {@code "WAW"}, {@code "StDiv"}, {@code "StEx"},
     * {@code "StFun"}, {@code "Str"}, {@code "A1"}..{@code "A4"}, {@code "M1"}..
     * {@code "M7"}, {@code "StAdd"}, {@code "StMul"}, {@code "DIV"} or
     * {@code "Dxx"} for divider counters). May be {@code null} for instructions
     * that haven't been seen by the CycleBuilder yet (e.g. when CycleBuilder
     * has not been wired in).
     */
    public String Stage;

    private Instruction() {}

    @JsIgnore()
    public static Instruction FromInstruction(InstructionInterface i) {
        if (i == null) {
            return null;
        }
        Instruction instruction = new Instruction();
        instruction.Name = i.getName();
        instruction.Code = i.getFullName();
        instruction.SerialNumber = i.getSerialNumber();
        instruction.Comment = i.getComment();
        instruction.BinaryRepresentation = i.getRepr().getBinString();
        instruction.OpCode = instruction.BinaryRepresentation.substring(0, 6);

        ParsedInstructionMetadata meta = i.getParsingMetadata();
        if (meta != null) {
            instruction.Address = meta.address;
            instruction.Line = meta.sourceLine;
        }

        return instruction;
    }
}