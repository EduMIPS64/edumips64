; delay-slot-jalr-link.s - test file for EduMIPS64.
;
; Edge case: same as delay-slot-jal-link.s but for JALR. The link value
; written into R31 must be deterministic across delay-slot OFF/ON.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r10, r0, 20         ; absolute address of `subroutine` (5 instructions in)
                nop                         ; padding so the JALR is at address 8
                jalr    r10                 ; JALR_PC = 8
label_slot:
                nop                         ; slot, address = 12
label_after_slot:
                break                       ; never executed if subroutine returns via halt
subroutine:
                daddi   r5, r31, 0          ; r5 := link value
                syscall 0
