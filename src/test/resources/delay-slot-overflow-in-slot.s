; delay-slot-overflow-in-slot.s - test file for EduMIPS64.
;
; Edge case: an instruction in the delay slot raises an arithmetic
; overflow during EX. EduMIPS64 must propagate the synchronous exception
; cleanly without leaving the pipeline in a half-advanced state.
;
; The slot here is ``dadd r4, r5, r5`` where r5 = 2^62; doubling it
; overflows the signed 64-bit range.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                ; r5 = 1 << 62, computed via two 31-bit shifts (dsll's
                ; shamt is a 5-bit immediate, so a single shift can do at
                ; most 31).
                daddi   r5, r0, 1
                dsll    r5, r5, 31
                dsll    r5, r5, 31
                nop                         ; spacing so the branch sees r5 ready (no RAW)
                nop
                beq     r1, r1, target      ; taken
                dadd    r4, r5, r5          ; overflow in EX — slot raises a SynchronousException
                break
target:
                syscall 0
