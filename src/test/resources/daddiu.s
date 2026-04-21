; daddiu.s - test file for EduMIPS64.
;
; Tests DADDIU: rt = rs + sign-extended immediate, 64-bit, no overflow trap.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        daddi  r1, r0, 100
        daddiu r2, r1, 23        ; 100 + 23 = 123
        daddi  r3, r0, 123
        bne    r2, r3, error

        ; Negative immediate (sign extension): 50 + (-17) = 33
        daddi  r4, r0, 50
        daddiu r5, r4, -17
        daddi  r6, r0, 33
        bne    r5, r6, error

        syscall 0

error:  break
        syscall 0
