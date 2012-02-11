; test-strcmp.s - tests for the strcmp routine

                .data
; Area for storing parameters for strcmp
strcmp_params:  .space 8
                .space 8
; Test strings
edu1:           .asciiz "EduMIPS64"
edu2:           .asciiz "EduMIPS64"
empty:          .space 8
test:           .asciiz "Test"

; Data for the error reporting routine
test_failed:    .asciiz "Test failed"
error_params:   .byte       2       ; stderr
error_addr:     .word64     0
                .byte       11

                .code

; Test 1: edu1 == edu2
    daddi       r1, r0, edu1
    daddi       r2, r0, edu2
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    bne         r1, r0, error

; Test 2: empty == empty
    daddi       r1, r0, empty
    daddi       r2, r0, empty
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    bne         r1, r0, error

; Test 3: empty < edu1
    daddi       r1, r0, empty
    daddi       r2, r0, edu1
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    daddi       r1, r1, 1
    bne         r1, r0, error

; Test 4: edu1 > empty
    daddi       r1, r0, edu1
    daddi       r2, r0, empty
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    daddi       r1, r1, -1
    bne         r1, r0, error

; Test 5: test > edu1
    daddi       r1, r0, test
    daddi       r2, r0, edu1
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    daddi       r1, r1, -1
    bne         r1, r0, error

; Test 6: edu1 < test
    daddi       r1, r0, edu1
    daddi       r2, r0, test
    daddi       r3, r0, 0
    sd          r1, strcmp_params(r3)
    daddi       r3, r0, 8
    sd          r2, strcmp_params(r3)
    daddi       r14 ,r0, strcmp_params
    jal         strcmp
    daddi       r1, r1, 1
    bne         r1, r0, error

    syscall     0

error:
    daddi       r1, r0, test_failed
    sd          r1, error_addr(r0)
    daddi       r14, r0, error_params
    syscall     4 
    syscall     0
    
#include utils/strcmp.s;
