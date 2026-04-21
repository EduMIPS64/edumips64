; movfd-semaphore-leak.s - minimal regression test for EduMIPS64.
;
; Exercises the MOVF.D / MOVT.D write-semaphore leak: with forwarding enabled,
; FPConditionalCC_DMoveInstructions did not call doWB() in EX, so the
; destination register's write semaphore was never decremented. Any later
; instruction that reads the destination would then stall forever waiting
; for the semaphore to clear.
;
; After MOVF.D writes f2, MOV.D reads f2. With the bug, this program hangs
; in ID forever and the JUnit timeout fires. With the fix, it reaches
; SYSCALL 0 and terminates cleanly.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
        .double 1.0
        .double 2.0
.code
        ldc1   f0, 0(r0)         ; f0 = 1.0
        ldc1   f1, 8(r0)         ; f1 = 2.0
        c.eq.d 0, f0, f1         ; cc0 = (1.0 == 2.0) = false
        movf.d f2, f1, 0         ; cc0 is false -> f2 = f1 (writes f2)
        mov.d  f3, f2            ; reads f2: hangs if f2's write semaphore leaked
        syscall 0
