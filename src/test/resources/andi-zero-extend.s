; andi-zero-extend.s - test file for EduMIPS64.
;
; Regression test for issue #1822: ANDI must accept zero-extended 16-bit
; immediates in the decimal range 32768..65535. A common use case is masking
; the high byte of the low halfword with the mask 65280 (0xFF00), which used to
; be rejected with IMMEDIATE_TOO_LARGE.
;
; This program is self-checking: it BREAKs (failing the test) on a wrong result.
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        ori    r1, r0, 65535     ; r1 = 0xFFFF

        andi   r2, r1, 65280     ; mask 0xFF00 -> r2 = 0xFF00
        dsrl   r3, r2, 8         ; -> 0xFF = 255
        daddi  r4, r0, 255
        bne    r3, r4, error

        andi   r5, r2, 255       ; 0xFF00 & 0x00FF -> 0
        bne    r5, r0, error

        syscall 0
error:  break
        syscall 0
