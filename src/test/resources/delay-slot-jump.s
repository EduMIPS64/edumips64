; delay-slot-jump.s - test file for EduMIPS64.
;
; Exercises the branch delay slot with an unconditional jump (J). The
; instruction after the J is the delay slot and must execute only when
; the delay slot is enabled.
;
; (c) 2024 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r2, r0, 0
                daddi   r3, r0, 0
                j       target
                daddi   r2, r2, 7       ; delay slot
                daddi   r3, r3, 99      ; must never execute
target:
                syscall 0
