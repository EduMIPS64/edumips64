; Test file for SYSCALL, GUI Version
;
; System calls read() and write() use different code when operating on
; stdin/stdout/stderr under the GUI or under the CLI. This is why we need
; separate tests for the GUI Version
;
; This test relies on manual input by the user of some strings.

                .data
test_string:    .asciiz      "TEST"
test_len:       .word        4 
                .text

; Test 1: read the "TEST" string from stdin (under the maximum length), check
; its length, check that it is equal to "TEST" and write it back to standard 
; output

                

; Test 2: read a 
