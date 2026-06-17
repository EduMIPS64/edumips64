; delay-slot-branch.s - test file for EduMIPS64.
;
; Exercises the branch delay slot. The instruction immediately after the
; branch (the "delay slot") increments R2; when the simulator is configured
; with the delay slot disabled (the default) that increment must not be
; observed, but when the delay slot is enabled the increment must take
; effect regardless of whether the branch is taken.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                daddi   r3, r0, 0
                beq     r1, r1, target  ; taken: r1 == r1
                daddi   r2, r2, 1       ; delay slot: should execute only if delay slot is enabled
                daddi   r3, r3, 99      ; must never execute (squashed by the taken branch)
target:
                syscall 0
