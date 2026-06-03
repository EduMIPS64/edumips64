; delay-slot-halt-in-slot.s - test file for EduMIPS64.
;
; Edge case: a HALT instruction placed in the delay slot of a taken
; branch. Same expected outcome as syscall-in-slot: the program
; terminates after executing the slot; the branch target never runs.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                beq     r1, r1, target      ; taken
                halt                        ; HALT in the delay slot: terminate
                break
target:
                daddi   r2, r2, 42          ; must NOT execute
                break
