; delay-slot-jr-in-jal-slot.s - test file for EduMIPS64.
;
; Edge case: a JR placed in the delay slot of a JAL ("tail call via jr in
; delay slot"). MIPS architecture: UNPREDICTABLE.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r5, r0, 24          ; address of `target2` (computed by hand: instr 6 * 4)
                nop
                nop
                jal     target1
                jr      r5                  ; JR IN the delay slot
                break
target1:
                syscall 0
target2:
                break
