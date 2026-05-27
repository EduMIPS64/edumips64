/*
 * InvalidDelaySlotException.java
 *
 * Exception of the MIPS64 Instruction Set: thrown when a control-transfer
 * instruction (branch, jump, JAL, JR, JALR, …) is fetched into the
 * architectural branch delay slot of another control-transfer instruction.
 *
 * The MIPS R4000 User's Manual §3.1.2 and the MIPS64 Architecture for
 * Programmers Vol. II both classify this situation as UNPREDICTABLE.
 * Production MIPS assemblers reject the pattern or emit a warning;
 * EduMIPS64 raises this exception instead of silently producing
 * implementation-defined state, so students see exactly what is wrong.
 *
 * (c) 2026 EduMIPS64 project
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */

package org.edumips64.core.is;

/**
 * Raised by the CPU at run time when a branch or jump instruction is
 * detected in the delay slot of another branch or jump while the
 * branch-delay-slot feature is enabled. On real MIPS hardware this is
 * UNPREDICTABLE; EduMIPS64 reports it as a fatal, deterministic error so
 * the offending program never silently corrupts architectural state.
 */
public class InvalidDelaySlotException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidDelaySlotException(String slotInstrName, long branchPC) {
        super("Control-transfer instruction '" + slotInstrName
                + "' found in the branch delay slot of the instruction at PC 0x"
                + Long.toHexString(branchPC)
                + ". On MIPS this is UNPREDICTABLE (see MIPS R4000 User's Manual §3.1.2).");
    }
}
