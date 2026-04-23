; lwc1-swc1.s - test file for EduMIPS64.
;
; Tests LWC1 (load word to FPR) and SWC1 (store word from FPR).
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.data
src:    .word32 12345
dst:    .word32 0
.code
        lwc1 f1, src(r0)         ; load word 12345 into low 32 bits of f1
        swc1 f1, dst(r0)         ; store f1's low 32 bits to dst

        lw   r1, dst(r0)         ; read the word back
        addi r2, r0, 12345
        bne  r1, r2, error

        ; Round-trip via MFC1 to check the FPR also holds the value.
        mfc1 r3, f1
        bne  r3, r2, error

        syscall 0

error:  break
        syscall 0
