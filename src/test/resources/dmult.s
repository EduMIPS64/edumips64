; dmult.s - test file for EduMIPS64.
;
; Executes dmult and tests -1 * -1 = 1, -2^63 * -2^63 = 2^126, -2^63 * 2^62 = -2^125
; and (2^63-1)^2
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
    .word64 0x8000000000000000 ;loaded as -2^63
    .word64 0x4000000000000000 ;high part of 2^126, also 2^62
    .word64 0xE000000000000000 ;high part of -2^125
    .word64 0x7FFFFFFFFFFFFFFF 
    .word64 0x3FFFFFFFFFFFFFFF ;high part of (2^63-1)^2

.code
    ;-1 * -1
    addi r1, r0, -1
    dmult r1, r1
    mfhi r2
    mflo r3
    addi r4, r0, 1
    bne r2, r0, error
    bne r3, r4, error
    ;(-2^63)^2
    ld r1, 0(r0)
    ld r2, 8(r0)
    dmult r1, r1
    mfhi r3
    mflo r4
    bne r3, r2, error
    bne r4, r0, error
    ;-2^63 * 2^62
    dmult r1, r2
    mfhi r3
    mflo r4
    ld r5, 16(r0)
    bne r5, r3, error
    bne r4, r0, error
    ;(2^63-1)^2
    ld r1, 24(r0)
    addi r2, r0, 1
    ld r3, 32(r0)
    dmult r1, r1
    mflo r4
    mfhi r5
    bne r2, r4, error
    bne r3, r5, error
    syscall 0

error:
    break
    syscall 0
    
