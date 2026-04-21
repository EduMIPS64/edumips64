; movfd.s - test file for EduMIPS64.
;
; Tests MOVF.D: if FCSR[cc] == 0 (condition is false), fd = fs.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 1.0
        .double 2.0
        .double 7.0
.code
        ldc1   f1, 0(r0)         ; f1 = 1.0
        ldc1   f2, 8(r0)         ; f2 = 2.0
        ldc1   f3, 16(r0)        ; f3 = 7.0 (sentinel, should change)
        ldc1   f4, 16(r0)        ; f4 = 7.0 (sentinel, should NOT change)

        ; Condition code 1 set to FALSE (1 == 2 is false).
        c.eq.d 1, f1, f2
        movf.d f3, f2, 1         ; cc==0, should move: f3 = f2 = 2.0
        c.eq.d 2, f3, f2
        bc1f   2, error

        ; Condition code 3 set to TRUE (1 == 1 is true).
        c.eq.d 3, f1, f1
        movf.d f4, f1, 3         ; cc==1, should NOT move: f4 stays 7.0
        c.eq.d 4, f4, f1
        bc1t   4, error          ; f4 should NOT equal f1

        syscall 0

error:  break
        syscall 0
