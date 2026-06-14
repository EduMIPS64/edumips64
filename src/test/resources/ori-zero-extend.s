; ori-zero-extend.s - test file for EduMIPS64.
;
; Regression test for issue #1822: ORI must accept zero-extended 16-bit
; immediates in the decimal range 32768..65535 (legal MIPS64 range 0..65535).
;
; This program is self-checking: it BREAKs (failing the test) if ORI does not
; load the expected zero-extended value 40000 (0x9C40).
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        ori    r1, r0, 40000     ; r1 = 0x9C40 (zero-extended, upper 48 bits = 0)

        andi   r2, r1, 255       ; low byte  -> 0x40 = 64
        daddi  r3, r0, 64
        bne    r2, r3, error

        dsrl   r4, r1, 8         ; high byte -> 0x9C = 156 (proves upper bits are zero)
        daddi  r5, r0, 156
        bne    r4, r5, error

        syscall 0
error:  break
        syscall 0
