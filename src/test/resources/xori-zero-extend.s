; xori-zero-extend.s - test file for EduMIPS64.
;
; Regression test for issue #1822: XORI must accept zero-extended 16-bit
; immediates in the decimal range 32768..65535 (legal MIPS64 range 0..65535).
;
; This program is self-checking: it BREAKs (failing the test) on a wrong result.
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        ori    r1, r0, 40000     ; r1 = 0x9C40
        xori   r2, r1, 40000     ; x ^ x -> 0 (XORI must use the same zero-extension)
        bne    r2, r0, error

        xori   r3, r0, 50000     ; r3 = 0xC350 = 50000
        andi   r4, r3, 255       ; low byte -> 0x50 = 80
        daddi  r5, r0, 80
        bne    r4, r5, error

        dsrl   r6, r3, 8         ; high byte -> 0xC3 = 195
        daddi  r7, r0, 195
        bne    r6, r7, error

        syscall 0
error:  break
        syscall 0
