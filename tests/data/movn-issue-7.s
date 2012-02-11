; test case for MOVN
;
; checks for the regression of bug #9, in which (with forwarding disabled) if
; the predicate of the MOVN or MOVZ instruction is false, and the destination
; register RD must not be overwritten, the value of RD that MOVN/MOVZ get in
; the ID stage was rewritten to RD, thus overwriting other possible changes
; occurred to the register (like, in those examples, the DADDI instructions
; that write to the R3 register).
;
; Test case provided by Alessandro Bucceri

    .code
    ; MOVN rd, rs, rt
    ; Move Conditional on Not Zero
    ;
    ; if GPR[rt] != 0 then GPR[rd] <- GPR[rs]

    daddi   r1, r0, 0
    daddi   r2, r0, 1
    daddi   r3, r0, 2
    movn    r3, r2, r1      ; if (r1 != 0), r3 = r2
    
    ; expected result: r1 = 0; r2 = 1; r3 = 2

    syscall 0
