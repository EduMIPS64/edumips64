; addiu.s - test file for EduMIPS64.
;
; Tests ADDIU. The 16-bit signed immediate is added to rs, the 32-bit result
; is sign-extended and written to rt. No overflow exception is raised.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
        ; Positive immediate: 5 + 7 = 12
        addi  r1, r0, 5
        addiu r2, r1, 7
        addi  r3, r0, 12
        bne   r2, r3, error

        ; Negative immediate (sign extension): 10 + (-3) = 7
        addi  r4, r0, 10
        addiu r5, r4, -3
        addi  r6, r0, 7
        bne   r5, r6, error

        ; Adding to zero yields the sign-extended immediate.
        addiu r7, r0, -1
        daddi r8, r0, -1
        bne   r7, r8, error

        syscall 0

error:  break
        syscall 0
