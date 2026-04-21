; subu.s - test file for EduMIPS64.
;
; Tests SUBU: rd = rs - rt as 32-bit unsigned, result sign-extended, no overflow trap.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 15
        addi r2, r0, 4
        subu r3, r1, r2          ; 15 - 4 = 11
        addi r4, r0, 11
        bne  r3, r4, error

        ; Subtraction producing a negative value; result is sign extended.
        addi r5, r0, 3
        addi r6, r0, 10
        subu r7, r5, r6          ; 3 - 10 = -7
        addi r8, r0, -7
        bne  r7, r8, error

        syscall 0

error:  break
        syscall 0
