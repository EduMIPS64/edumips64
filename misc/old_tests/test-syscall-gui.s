; Test file for SYSCALL, GUI Version
; (c) 2011 Andrea Spadaccini, licensed under the GNU GPL v2 or later
;
; System calls read() and write() use different code when operating on
; stdin/stdout/stderr under the GUI or under the CLI. This is why we need
; separate tests for the GUI Version
;
; This test relies on manual input by the user of some strings.

                .data
params_read:    .space       8      ; file descriptor;
read_address:   .space       8      ; address of the memory location where the
                                    ; read string will be stored;
test_len:       .word        4      ; number of characters to read;
read_string:    .space       8
test_string:    .asciiz      "TEST"

                .text

; Test 1: read the "TEST" string from stdin, check its length, check that it
; is equal to "TEST" and write it back to standard output

    ; Read data from stdin
    sd          r0, params_read(r0)
    daddi       r1, r0, read_string
    sd          r1, read_address(r0)
    syscall     3

    ; Check read() return code
    daddi       r1, r1, 1
    beq         r1, r0, 

    ; Call strlen

; Test 2: read a 
    syscall 0

error:
    daddi       r1, r0, test_failed
    sd          r1, error_addr(r0)
    daddi       r14, r0, error_params
    syscall     4 
    syscall     0

#include utils/strlen.s;
#include utils/strcmp.s;
