; delay-slot-jump-in-slot.s - test file for EduMIPS64.
;
; Edge case: an unconditional jump placed in the delay slot of another
; unconditional jump. MIPS architecture: UNPREDICTABLE.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                j       target1
                j       target2             ; jump IN the delay slot
                break
target1:
                syscall 0
target2:
                break
