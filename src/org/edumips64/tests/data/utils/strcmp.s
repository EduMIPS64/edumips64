; strcmp.s - checks two null-terminated strings for equality
; (c) 2011 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; 
; Checks if two null-terminated strings contain the same characters
;
; Expects in r14 the address of the those parameters:
; * first string (s1)
; * second string (s2)
; 
; Returns in r1:
; * -1 if s1 < s2 (lexicographical comparison)
; * 0  if s1 == s2
; * 1  if s1 > s2

                    .data
save_r2:            .space 8
save_r3:            .space 8
save_r4:            .space 8
save_r5:            .space 8
_strcmp_sgn_mask:   .word64     0x7FFFFFFFFFFFFFFF

                    .code
strcmp:
    ; back-up registers
    sd      r2, save_r2(r0)
    sd      r3, save_r3(r0)
    sd      r4, save_r4(r0)
    sd      r5, save_r5(r0)

    ; Reset r1, that will contain the return value
    daddi   r1, r0, 0

    ; Get the address of the parameters
    ld      r2, 0(r14)      ; address of s1
    ld      r3, 8(r14)      ; address of s2


_strcmp_loop:
    ; get the two current bytes, b1 and b2
    lb      r4, 0(r2)
    lb      r5, 0(r3)

    ; increment pointers
    daddi   r2, r2, 1
    daddi   r3, r3, 1

    ; if b1 != b2 goto _strcmp_different
    bne     r4, r5, _strcmp_different

    ; if b1 == b2 == '\0', goto _end (r1 is already set to 0)
    beq     r4, r0, _strcmp_end

    ; if b1 == b2 and b2 != '\0', loop
    b       _strcmp_loop

_strcmp_different:
    ; r1 = r4 - r5
    dsub    r1, r4, r5

    ; r1 = signum(r1)   (using r2 as temp variable two times)
    ; it works by OR'ing the register with the value
    ; 0x7FFFFFFFFFFFFFFF, that is 0 followed by 63 1s.
    ; If r1 is negative, the result will be 0xFF..F (64 1s), because of two's
    ; complement arithmetic, while if it is positive it will be 0x7FF..F (the
    ; same value).
    ; Then shifting this value 62 bits to the right, thanks to sign extension,
    ; will yield 1 (0x00..1) if the value was positive and -1 (0xFF..F) if the
    ; value was negative.
    
    ld      r2, _strcmp_sgn_mask(r0)
    or      r1, r1, r2
    daddi   r2, r0, 62
    dsrav   r1, r1, r2
    
    b       _strcmp_end

_strcmp_end:
    ; restore registers
    ld      r2, save_r2(r0)
    ld      r3, save_r3(r0)
    ld      r4, save_r4(r0)
    ld      r5, save_r5(r0)

    ; Return
    jr      r31 

