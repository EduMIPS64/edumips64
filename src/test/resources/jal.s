; jal.s - tests for the JAL instruction
; (c) 2012 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; Executes a break instruction if the test fails
                .code
; Test jump capability of JAL
    jal         continue
    b           error       ; should never return here

continue:
    ; R31 should contain 4 (address of the second instruction)
    addi        r31, r31, -4
    bnez        r31, error
    syscall     0

error:
    break
