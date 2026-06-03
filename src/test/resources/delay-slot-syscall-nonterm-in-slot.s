; delay-slot-syscall-nonterm-in-slot.s - test file for EduMIPS64.
;
; Edge case: a NON-terminating SYSCALL (here SYSCALL 5, printf) placed in
; the delay slot of a taken branch. Unlike SYSCALL 0 / HALT, this kind of
; system call has no termination side-effect, so the educational choice
; is: execute the slot to completion (including its visible side effect
; and its WB write to R1), then continue execution at the branch target.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .data
fmt:            .asciiz "ok"
fs_addr:        .space  4
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                daddi   r5, r0, fmt
                sw      r5, fs_addr(r0)
                daddi   r14, r0, fs_addr
                beq     r1, r1, target          ; taken
                syscall 5                       ; non-terminating syscall in the delay slot
                break                           ; never executed
target:
                daddi   r2, r2, 42              ; MUST execute (syscall did not terminate)
                syscall 0
