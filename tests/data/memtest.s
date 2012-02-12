; memtest.s
;
; tests the behaviour of load/store instructions.
;
; Author: Andrea Spadaccini <andrea.spadaccini@gmail.com>

        .code
        ; BYTE TESTS
        ; ----------
        ; Write a signed value to memory location 0, and check that it is read
        ; properly by signed and unsigned load byte instructions.
        daddi   r1, r0, -20
        sb      r1, 0(r0)
        lb      r2, 0(r0)
        lbu     r3, 0(r0)
        bne     r2, r1, err     ; if r2 != r1; goto err
        beq     r3, r1, err     ; if r2 == r1; goto err

        ; Do a basic test (write/read unsigned) for the rest of the first
        ; double word of memory.
        daddi   r1, r0, 20
        daddi   r10, r0, 7
loopB:  sb      r1, 0(r10)
        lb      r2, 0(r10) 
        bne     r2, r1, err     ; if r2 != r1; goto err
        daddi   r10, r10, -1
        bnez    r10, loopB

        ; HALF WORD TESTS (menza parola)
        ; ---------------
        ; Write a signed value to memory location 8, and check that it is read
        ; properly by signed and unsigned load half word instructions.
        daddi   r1, r0, -30001
        sh      r1, 8(r0)
        lh      r2, 8(r0)
        lhu     r3, 8(r0)
        bne     r2, r1, err     ; if r2 != r1; goto err
        beq     r3, r1, err     ; if r2 == r1; goto err

        ; Do a basic test (write/read unsigned) for the rest of the second
        ; double word of memory.
        daddi   r1, r0, 30001

        ; we iterate from 3 to 1, and duplicate the index in order to get to
        ; memory locations 14, 12, 10 by adding 6, 4 and 2 to 8.
        daddi   r10, r0, 3
loopH:  daddi   r11, r0, 0
        dadd    r11, r10, r10
        sh      r1, 8(r11)
        lh      r2, 8(r11) 
        bne     r2, r1, err     ; if r2 != r1; goto err
        daddi   r10, r10, -1
        bnez    r10, loopH

        ; WORD TESTS
        ; ----------
        ; Write a signed value to memory locations 16 and 20, and check that
        ; they are read properly by signed and unsigned load word instructions.
        daddi   r1, r0, -30001
        sw      r1, 16(r0)
        lw      r2, 16(r0)
        lwu     r3, 16(r0)
        sw      r1, 20(r0)
        lw      r4, 20(r0)
        lwu     r5, 20(r0)
        bne     r2, r1, err     ; if r2 != r1; goto err
        beq     r3, r1, err     ; if r2 == r1; goto err
        bne     r4, r1, err     ; if r4 != r1; goto err
        beq     r5, r1, err     ; if r5 == r1; goto err

        ; DOUBLE WORD TESTS
        ; -----------------
        ; Write a signed value to memory location 24, and check that
        ; it is read properly by load double word.
        daddi   r1, r0, -30001
        sd      r1, 24(r0)
        ld      r2, 24(r0)
        bne     r2, r1, err     ; if r2 != r1; goto err

        syscall 0

err:    break
