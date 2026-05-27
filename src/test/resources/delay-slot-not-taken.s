; delay-slot-not-taken.s - test file for EduMIPS64.
;
; Exercises a NOT-taken branch with the delay slot. The instruction right
; after the (not-taken) branch must execute regardless of whether the delay
; slot is enabled, since the branch falls through to it. The point of this
; test is to make sure the delay-slot pipeline rewiring does not break
; not-taken branches.
;
; (c) 2024 EduMIPS64 project
                .code
                daddi   r1, r0, 1
                daddi   r2, r0, 0
                beq     r1, r0, error   ; not taken: r1 != r0
                daddi   r2, r2, 5       ; falls through and executes
                syscall 0
error:
                break
