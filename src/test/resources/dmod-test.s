; dmod-test.s - test file for DMOD instruction
;
; Tests the DMOD rd, rs, rt instruction (signed modulo)
;
; (c) 2025 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.

.code
; Test positive modulo: 17 % 5 = 2
daddi r1, r0, 17
daddi r2, r0, 5
dmod r3, r1, r2          ; r3 should contain 2
daddi r4, r0, 2
bne r3, r4, failure      ; If r3 != 2, jump to failure

; Test negative dividend modulo: -17 % 5 = -2
daddi r1, r0, -17
daddi r2, r0, 5
dmod r3, r1, r2          ; r3 should contain -2
daddi r4, r0, -2
bne r3, r4, failure      ; If r3 != -2, jump to failure

; Test with negative divisor: 17 % -5 = 2
daddi r1, r0, 17
daddi r2, r0, -5
dmod r3, r1, r2          ; r3 should contain 2
daddi r4, r0, 2
bne r3, r4, failure      ; If r3 != 2, jump to failure

; Test zero remainder: 20 % 5 = 0
daddi r1, r0, 20
daddi r2, r0, 5
dmod r3, r1, r2          ; r3 should contain 0
bne r3, r0, failure      ; If r3 != 0, jump to failure

; All tests passed
syscall 0

failure:
; If we get here, a test failed
break
syscall 0
