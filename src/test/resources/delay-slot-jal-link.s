; delay-slot-jal-link.s - test file for EduMIPS64.
;
; Edge case: verifies the link-register value written by JAL. MIPS spec:
; "R31 ← address of the instruction *after* the delay slot" (= JAL_PC + 8).
; EduMIPS64 historically stores JAL_PC + 4 (the address of the instruction
; immediately following the JAL, i.e. the slot itself). With the delay
; slot disabled this is the architecturally correct return point because
; the slot is squashed; with the delay slot enabled the slot executes,
; so the correct return point would be JAL_PC + 8.
;
; This test documents the current behaviour by pinning down the value
; written into R31 and comparing it against the address of the slot
; (`label_slot`).
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                jal     subroutine          ; JAL_PC = 0
label_slot:
                nop                         ; slot, address = 4
label_after_slot:
                break                       ; address = 8: never reached when delay slot is OFF
subroutine:
                ; R5 := simulator-reported link value (R31)
                daddi   r5, r31, 0
                syscall 0
