; trap.s - test file for EduMIPS64.
;
; Tests TRAP, a deprecated alias for SYSCALL. TRAP 0 terminates the program.
; If the TRAP exits cleanly we pass; the BREAK after it should never execute.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 1
        addi r2, r0, 1
        bne  r1, r2, error       ; sanity check
        trap 0                   ; same as syscall 0: terminate

        break                    ; should never be reached
error:  break
