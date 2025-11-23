; ddiv-both-forms-test.s - test file for both DDIV instruction forms
;
; Tests both the legacy DDIV rs, rt (2-param) form and the new DDIV rd, rs, rt (3-param) form
;
; (c) 2025 EduMIPS64 project

.code
; Test 2-param DDIV: divide 100 / 4, result in LO
daddi r1, r0, 100
daddi r2, r0, 4
ddiv r1, r2          ; quotient=25 in LO, remainder=0 in HI
mflo r3              ; r3 should contain 25
daddi r4, r0, 25
bne r3, r4, failure

; Test 3-param DDIV: divide 100 / 4 = 25
daddi r5, r0, 100
daddi r6, r0, 4
ddiv r7, r5, r6      ; r7 should contain 25
daddi r8, r0, 25
bne r7, r8, failure

; Test 3-param DDIV with negative: -100 / 4 = -25
daddi r5, r0, -100
daddi r6, r0, 4
ddiv r7, r5, r6      ; r7 should contain -25
daddi r8, r0, -25
bne r7, r8, failure

; All tests passed
syscall 0

failure:
; If we get here, a test failed
break
syscall 0
