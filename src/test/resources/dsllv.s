; dsllv.s - test file for EduMIPS64.
;
; Executes dsllv on various numbers
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
.data
    .word64 0x5555555555555555
    .word64 0xAAAAAAAAAAAAAAAA ;<<1
    .word64 0x5555555500000000 ;<<32
    .word64 0xAAAAAAAA00000000 ;<<33
    .word64 0x8000000000000000 ;<<63

.code
    ld r1, 0(r0)
    ld r2, 8(r0)
    addi r3, r0, 1
    dsllv r4, r1, r3
    bne r4, r2, error
    ld r2, 16(r0)
    addi r3, r0, 32
    dsllv r4, r1, r3
    bne r4, r2, error
    ld r2, 24(r0)
    addi r3, r0, 33
    dsllv r4, r1, r3
    bne r4, r2, error
    ld r2, 32(r0)
    addi r3, r0, 63
    dsllv r4, r1, r3
    bne r4, r2, error
    syscall 0

error:
    break
    syscall 0
