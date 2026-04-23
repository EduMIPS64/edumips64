; delay-slot.s - test file for EduMIPS64.
;
; Test for the delay slot feature. When the delay slot option is enabled,
; the instruction following a jump/branch (here DADDI r2, r0, 1) must be
; executed as part of the normal program flow. When delay slot mode is off,
; the same instruction is flushed out of the pipeline and must not run.
;
; To distinguish the two cases this program uses r2 as a flag that is set
; to 1 only by the instruction placed in the delay slot. After the jump
; target we move r2 into r1 and issue SYSCALL 0 to stop the simulator,
; so the test can inspect r1 to check whether the delay slot ran.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 0         ; r1 = 0 (will be overwritten at end)
                daddi   r2, r0, 0         ; r2 = 0 (delay-slot flag)
                j       target
                daddi   r2, r0, 1         ; delay slot: runs only if feature is on
                daddi   r2, r0, 99        ; must never run (would be fetched after target)
target:         daddi   r1, r2, 0         ; r1 = r2 (1 if delay slot ran, 0 otherwise)
                syscall 0
