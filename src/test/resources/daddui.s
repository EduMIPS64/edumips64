; daddui.s - test file for EduMIPS64.
;
; Tests DADDUI, which is defined as an alias for DADDIU.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        daddi  r1, r0, 40
        daddui r2, r1, 2         ; 40 + 2 = 42
        daddi  r3, r0, 42
        bne    r2, r3, error

        daddui r4, r0, -1        ; 0 + (-1) = -1 (sign-extended)
        daddi  r5, r0, -1
        bne    r4, r5, error

        syscall 0

error:  break
        syscall 0
