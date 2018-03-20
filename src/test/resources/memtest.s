; memtest.s - test file for EduMIPS64.
;
; Tests the behaviour of load/store instructions.

; Errors are handled by calling BREAK, that in the unit tests running code is
; treated as an error.
;
; (c) 2018 Andrea Spadaccini and the EduMIPS64 team
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
; 
; This program is free software; you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 2 of the License, or
; (at your option) any later version.
; 
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
; 
; You should have received a copy of the GNU General Public License
; along with this program; if not, write to the Free Software
; Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
