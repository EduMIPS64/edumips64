; mfc1.s - test file for EduMIPS64.
;
; Tests MFC1: copies a word from an FPU register to a GPR, sign-extending.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        addi r1, r0, 1234
        mtc1 r1, f1              ; put 1234 into the low word of f1
        mfc1 r2, f1              ; read it back into r2
        bne  r1, r2, error

        ; Sign-extension check: a negative word should be sign-extended on read.
        addi r3, r0, -1
        mtc1 r3, f2
        mfc1 r4, f2
        bne  r3, r4, error

        syscall 0

error:  break
        syscall 0
