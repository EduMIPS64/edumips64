; delay-slot-div0-in-slot.s - test file for EduMIPS64.
;
; Edge case: a divide-by-zero is placed in the delay slot of a taken
; branch. Integer DIV in EduMIPS64 raises a SynchronousException at EX.
; The branch has already retired by the time the exception fires; the
; simulator must report the exception without crashing.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r5, r0, 7
                beq     r1, r1, target      ; taken
                ddiv    r5, r0              ; divide by zero in delay slot
                break
target:
                syscall 0
