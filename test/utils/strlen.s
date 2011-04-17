; strlen.s - computes the length of a string
; (c) 2011 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; 
; Computes the length of a string, not including the ending '\0'
;
; Expects in r14 the address of the string and returns its length
; in r1
                .data
str_address:    .space 8
save_r2:        .space 8

                .text
strlen:
    ; Our temporary variables are r1 and r2. r1 will be the index of the
    ; current character, while r2 will be the current character.

    ; back-up and reset r2
    sd      r2, save_r2(r0)
    daddi   r2, r0, 0

    ; move to r1 the address of the string
    daddi   r1, r14, 0

_loop:
    lb      r2, 0(r1)
    daddi   r1, r1, 1
    bne     r2, r0, _loop
    
_end:
    ; compute the length
    daddi   r1, r1, -1 
    dsub    r1, r1, r14 
    
    ; Restore r2
    ld      r2, save_r2(r0)
    
    ; Return
    jr      r31
