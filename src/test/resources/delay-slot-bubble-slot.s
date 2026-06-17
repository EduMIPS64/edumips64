; delay-slot-bubble-slot.s - test file for EduMIPS64.
;
; Edge case (sanity): the delay slot is a NOP, which is the canonical
; well-defined pattern. With or without delay-slot support, observable
; register state must be identical.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                beq     r1, r1, target      ; taken
                nop                         ; canonical slot
                break
target:
                daddi   r2, r2, 11
                syscall 0
