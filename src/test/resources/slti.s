; slti.s - test file for EduMIPS64.
;
; Tests SLTI: rt = 1 if rs < sign-extended immediate (signed), else 0.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 5
        slti r2, r1, 10          ; 5 < 10 -> 1
        addi r3, r0, 1
        bne  r2, r3, error

        slti r4, r1, 5           ; 5 < 5 -> 0
        bne  r4, r0, error

        slti r5, r1, 1           ; 5 < 1 -> 0
        bne  r5, r0, error

        addi r6, r0, -5
        slti r7, r6, -3          ; -5 < -3 -> 1 (signed)
        bne  r7, r3, error

        syscall 0

error:  break
        syscall 0
