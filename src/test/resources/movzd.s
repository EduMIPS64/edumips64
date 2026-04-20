; movzd.s - test file for EduMIPS64.
;
; Tests MOVZ.D: if rt == 0, fd = fs.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 3.0
        .double 9.0
.code
        ldc1   f1, 0(r0)         ; f1 = 3.0 (source)
        ldc1   f2, 8(r0)         ; f2 = 9.0 (destination that should change)
        ldc1   f3, 8(r0)         ; f3 = 9.0 (destination that should NOT change)

        movz.d f2, f1, r0        ; r0 == 0, move: f2 = f1 = 3.0
        c.eq.d 0, f2, f1
        bc1f   0, error

        addi   r1, r0, 7         ; r1 != 0
        movz.d f3, f1, r1        ; should NOT move: f3 stays 9.0
        c.eq.d 1, f3, f1
        bc1t   1, error

        syscall 0

error:  break
        syscall 0
