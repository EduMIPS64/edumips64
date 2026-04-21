; addu.s - test file for EduMIPS64.
;
; Tests ADDU: rd = rs + rt as 32-bit unsigned, result sign-extended, no overflow trap.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 4
        addi r2, r0, 9
        addu r3, r1, r2          ; 4 + 9 = 13
        addi r4, r0, 13
        bne  r3, r4, error

        ; Addition with a negative register; no overflow trap even at boundaries.
        addi r5, r0, -1
        addi r6, r0, 1
        addu r7, r5, r6          ; -1 + 1 = 0 (32-bit sign-extended)
        bne  r7, r0, error

        syscall 0

error:  break
        syscall 0
