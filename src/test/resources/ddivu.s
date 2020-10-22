; ddivu.s - test file for EduMIPS64.
;
; Executes ddivu and tests for correctness
;
; (c) 2020 Leopold Eckert and the EduMIPS64 team
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
.data   .word64 0x8000000000000000 ;2^63, or 9223372036854775808
        .word64 0xFFFFFFFFFFFFFFFF ;2^64 - 1
        .word64 0x7FFFFFFFFFFFFFFF ;2^63 - 1
        .word64 0x80000000 ;2^31
        .word64 0x100000000 ;2^32

.code
        ld    r1, 0(r0)
        ld    r2, 8(r0)
        ld    r3, 16(r0)
        addi  r4, r0, 1
        ddivu r2, r1
        mfhi  r5
        mflo  r6
        bne   r4, r6, error
        bne   r3, r5, error
        ld    r3, 24(r0)
        ld    r4, 32(r0)
        ddivu r1, r3
        mfhi  r5
        mflo  r6
        bne   r5, r0, error
        bne   r6, r4, error
        syscall 0

error:  break
        syscall 0
        
