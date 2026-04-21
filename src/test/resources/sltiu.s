; sltiu.s - test file for EduMIPS64.
;
; Tests SLTIU: rt = 1 if rs < sign-extended immediate (compared as unsigned), else 0.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi  r1, r0, 5
        sltiu r2, r1, 10         ; 5 < 10 -> 1 (unsigned)
        addi  r3, r0, 1
        bne   r2, r3, error

        sltiu r4, r1, 5          ; 5 < 5 -> 0
        bne   r4, r0, error

        ; -1 sign-extended is 0xFFFFFFFFFFFFFFFF, which is the largest unsigned value.
        ; So 5 < -1 when compared as unsigned is true.
        sltiu r5, r1, -1
        bne   r5, r3, error

        ; Conversely, -1 as rs (which is 0xFFFFFFFFFFFFFFFF unsigned) is not less than 0.
        addi  r6, r0, -1
        sltiu r7, r6, 0
        bne   r7, r0, error

        syscall 0

error:  break
        syscall 0
