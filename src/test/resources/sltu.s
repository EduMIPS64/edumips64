; sltu.s - test file for EduMIPS64.
;
; Tests SLTU: rd = 1 if rs < rt (compared as unsigned), else 0.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 3
        addi r2, r0, 8
        sltu r3, r1, r2          ; 3 < 8 -> 1
        addi r4, r0, 1
        bne  r3, r4, error

        sltu r5, r2, r1          ; 8 < 3 -> 0
        bne  r5, r0, error

        sltu r6, r1, r1          ; 3 < 3 -> 0
        bne  r6, r0, error

        ; Unsigned comparison: -1 is 0xFFFF...F > 1, so 1 < -1 is true (unsigned).
        addi r7, r0, -1
        addi r8, r0, 1
        sltu r9, r8, r7
        bne  r9, r4, error

        ; And -1 < 1 is false (unsigned).
        sltu r10, r7, r8
        bne  r10, r0, error

        syscall 0

error:  break
        syscall 0
