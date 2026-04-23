; movd.s - test file for EduMIPS64.
;
; Tests MOV.D: copies one FP (double) register into another.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 2.5
.code
        ldc1   f1, 0(r0)         ; f1 = 2.5
        mov.d  f2, f1            ; f2 = f1

        c.eq.d 0, f1, f2         ; expect f1 == f2
        bc1f   0, error
        syscall 0

error:  break
        syscall 0
