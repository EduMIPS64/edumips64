; strlen.s - computes the length of a null-terminated string
; (c) 2011 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; 
; Computes the length of a string, not including the ending '\0'
;
; Expects in r14 the address of those parameters:
; - string
;
; Returns in r1 the length of the string.

                .data
str_address:    .space 8
save_r2:        .space 8
save_r3:        .space 8

                .text
strlen:
    ; Our temporary variables are r1, r2 and r3. 
    ; r1 will be the index of the current character
    ; r2 will be the current character
    ; r3 will be used to hold the initial parameters address

    ; back-up and reset r2 and r3
    sd      r2, save_r2(r0)
    daddi   r2, r0, 0
    sd      r3, save_r3(r0)
    daddi   r3, r0, 0

    ; Read and save parameters address
    ld      r1, 0(r14) 
    dadd    r3, r1, r0

_loop:
    lb      r2, 0(r1)
    daddi   r1, r1, 1
    bne     r2, r0, _loop
    
_end:
    ; compute the length
    daddi   r1, r1, -1 
    dsub    r1, r1, r3
    
    ; Restore r2 and r3
    ld      r2, save_r2(r0)
    ld      r3, save_r3(r0)
    
    ; Return
    jr      r31
