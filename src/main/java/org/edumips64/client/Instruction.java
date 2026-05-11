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
     * Last state recorded for this instruction by {@code CycleBuilder} for the
     * most recently completed CPU cycle, or {@code null} if the instruction
     * hasn't been seen by the {@code CycleBuilder} yet (e.g. when
     * {@code CycleBuilder} has not been wired in). When {@code Stage} is
     * {@code "DIV_COUNT"}, the actual counter value (0..24) is exposed
     * separately via {@link #DivCount}; for any other stage,
     * {@link #DivCount} is {@code -1}.
     *
     * <p>Wire format: a {@code String} matching {@link CycleState#name()}
     * (e.g. {@code "ID"}, {@code "RAW"}, {@code "StDiv"}). We deliberately
     * use the string form across the JS boundary because GWT-compiled
     * {@code @JsType} enums are exposed as objects with mangled internal
     * field names — {@code .name()} is not reliably reachable from JS.
     * On the Java side, conversion goes through the typed {@link CycleState}
     * enum (see {@code ResultFactory.wrap()}), so type safety is preserved
     * at construction.
     */
    public String Stage;

    /**
     * Companion to {@link #Stage}: the FP divider's per-cycle counter when
     * {@code Stage == CycleState.DIV_COUNT} (in the range {@code [0, 24]}),
     * or {@code -1} otherwise. Kept as a separate field so {@link CycleState}
     * does not need 25 enum constants for what is just a parameterised tag;
     * see the class-level Javadoc on {@link CycleState}.
     */
    public int DivCount = -1;

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