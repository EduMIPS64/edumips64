; dmul-ddiv-test.s - test file for DMUL and DDIV instructions
;
; Tests the new DMUL rd,rs,rt and DDIV rd,rs,rt instructions
;
; (c) 2024 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.

.code
; Test DMUL: multiply 10 * 5 = 50
daddi r1, r0, 10
daddi r2, r0, 5
dmul r3, r1, r2          ; r3 should contain 50
daddi r4, r0, 50
bne r3, r4, failure      ; If r3 != 50, jump to failure

; Test DMUL with negative numbers: -10 * 5 = -50
daddi r1, r0, -10
daddi r2, r0, 5
dmul r3, r1, r2          ; r3 should contain -50
daddi r4, r0, -50
bne r3, r4, failure      ; If r3 != -50, jump to failure

; Test DDIV: divide 100 / 4 = 25
daddi r1, r0, 100
daddi r2, r0, 4
ddiv r3, r1, r2          ; r3 should contain 25
daddi r4, r0, 25
bne r3, r4, failure      ; If r3 != 25, jump to failure

; Test DDIV with negative numbers: -100 / 4 = -25
daddi r1, r0, -100
daddi r2, r0, 4
ddiv r3, r1, r2          ; r3 should contain -25
daddi r4, r0, -25
bne r3, r4, failure      ; If r3 != -25, jump to failure

; All tests passed
syscall 0

failure:
; If we get here, a test failed
break
syscall 0
