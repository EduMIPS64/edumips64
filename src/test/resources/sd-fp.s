; sd-fp.s - test file for EduMIPS64.
;
; Tests S.D: stores a double-precision FP value from an FPR to memory.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 1.5
        .double 0.0
.code
        ldc1   f1, 0(r0)         ; f1 = 1.5
        s.d    f1, 8(r0)         ; write f1 to the second .double slot
        ldc1   f2, 8(r0)         ; read it back into f2

        c.eq.d 0, f1, f2         ; f1 should equal f2
        bc1f   0, error
        syscall 0

error:  break
        syscall 0
