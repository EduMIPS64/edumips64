; cvtwd.s - test file for EduMIPS64.
;
; Tests CVT.W.D: converts a double-precision FP value to a 32-bit fixed-point
; integer, stored in the low 32 bits of the destination FPR.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 42.0
.code
        ldc1    f1, 0(r0)        ; f1 = 42.0 (double)
        cvt.w.d f2, f1           ; f2 = 42 (word, as low 32 bits of f2)
        mfc1    r1, f2           ; move the word into a GPR
        addi    r2, r0, 42
        bne     r1, r2, error

        syscall 0

error:  break
        syscall 0
