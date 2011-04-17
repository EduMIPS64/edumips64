; test-strlen.s - tests for the strlen routine
                .data
; Parameter for strlen
strlen_params:  .space 8

edu_str:        .asciiz "EduMIPS64"
empty_string:   .space 8

; Data for the error reporting routine
test_failed:    .asciiz "Test failed"
error_params:   .byte       2       ; stderr
error_addr:     .word64     0
                .byte       11

                .code

; Test 1: compute the length of an empty string. If it is different from 0,
;         fail

    daddi       r1, r0, empty_string
    sd          r1, strlen_params(r0)
    daddi       r14, r0, strlen_params
    jal         strlen
    bne         r1, r0, error

; Test 2: compute the length of the "EduMIPS64" string. If it is different
;         from 9, fail.

    daddi       r1, r0, edu_str
    sd          r1, strlen_params(r0)
    daddi       r14, r0, strlen_params
    jal         strlen
    daddi       r1, r1, -9
    bne         r1, r0, error

    syscall     0

error:
    daddi       r1, r0, test_failed
    sd          r1, error_addr(r0)
    daddi       r14, r0, error_params
    syscall     4 
    syscall     0
    
#include utils/strlen.s;
