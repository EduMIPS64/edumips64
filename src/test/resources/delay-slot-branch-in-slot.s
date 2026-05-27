; delay-slot-branch-in-slot.s - test file for EduMIPS64.
;
; Edge case: a conditional branch placed in the delay slot of another
; conditional branch. The MIPS architecture defines this situation as
; UNPREDICTABLE (MIPS R4000 User's Manual §3.1.2, MIPS64 Vol. II "Control
; Transfer Instructions in a Branch Delay Slot result in UNPREDICTABLE
; behavior").
;
; This program contains one such pattern. EduMIPS64 must either reject it
; or behave deterministically rather than silently corrupting state. With
; the delay slot disabled the inner branch is simply squashed so the
; program completes normally; with the delay slot enabled the simulator
; should raise a clearly-labelled InvalidDelaySlotException.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 1
                beq     r1, r1, target1     ; taken
                beq     r2, r2, target2     ; branch IN the delay slot
                break                       ; never reached either way
target1:
                syscall 0
target2:
                break
