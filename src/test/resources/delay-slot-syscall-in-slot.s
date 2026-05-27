; delay-slot-syscall-in-slot.s - test file for EduMIPS64.
;
; Edge case: a terminating SYSCALL (syscall 0) placed in the delay slot of
; a taken branch. On a real MIPS this would generate an exception with
; EPC pointing at the branch and Cause.BD=1; EduMIPS64 has no CP0, so the
; educational choice is: execute the slot, then terminate, mirroring the
; existing BREAK-in-slot semantics. The branch target must not execute.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                beq     r1, r1, target      ; taken
                syscall 0                   ; SYSCALL in the delay slot: terminate
                break                       ; never executed
target:
                daddi   r2, r2, 42          ; must NOT execute (terminated in slot)
                break
