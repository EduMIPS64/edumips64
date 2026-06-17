; delay-slot-break-in-slot.s - test file for EduMIPS64.
;
; Edge case: a BREAK instruction placed in the delay slot of a taken
; branch. EduMIPS64 behavior (already implemented in CPU.java around the
; JumpException handler):
;   * with delay slot disabled: the BREAK is squashed and silently
;     ignored — the simulator continues past the branch.
;   * with delay slot enabled:  the BREAK is propagated as a real
;     BreakException so the test framework observes it.
; The dedicated unit test asserts both directions.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                beq     r1, r1, target      ; taken
                break                       ; BREAK in the delay slot
                break                       ; sequentially next; never executed
target:
                daddi   r2, r2, 7           ; only reached with delay-slot OFF
                syscall 0
