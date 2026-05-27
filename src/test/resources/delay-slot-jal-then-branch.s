; delay-slot-jal-then-branch.s - test file for EduMIPS64.
;
; Edge case: JAL followed by an unconditional branch as the slot. On a
; real MIPS this is "branch in branch delay slot" → UNPREDICTABLE; an
; assembler typically warns. EduMIPS64 must either reject it or behave
; deterministically. This is the same pattern that motivated the comment
; inside testJalWithDelaySlot in EndToEndTests.java.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                jal     subroutine
                b       error               ; branch in JAL's delay slot — UNPREDICTABLE
                break
subroutine:
                syscall 0
error:
                break
