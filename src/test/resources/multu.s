; multu.s - test file for EduMIPS64.
;
; Executes multu and tests  (2^32-1)^2, (2^31)^2 = 2^62, 2^31 * 2^30 = 2^61
; and (2^32-1)^2
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
    .word32 -2
    .word32 -2147483648 ;loaded as 2^31
    .word32  0x40000000 ;high part of 2^62, also 2^30
    .word32  536870912 ;high part of 2^61
    .word32  0x7FFFFFFF 
    .word32  0x3FFFFFFF ;high part of (2^63-1)^2

.code
    ;(2^32-1)^2
    addi r1, r0, -1
    multu r1, r1
    mfhi r2
    mflo r3
    addi r4, r0, 1
    lwu r5, 0(r0)
    bne r2, r5, error
    bne r3, r4, error
    ;(-2^31)^2
    lwu r1, 8(r0)
    lwu r2, 16(r0)
    multu r1, r1
    mfhi r3
    mflo r4
    bne r3, r2, error
    bne r4, r0, error
    ;-2^31 * 2^30
    multu r1, r2
    mfhi r3
    mflo r4
    lwu r5, 24(r0)
    bne r5, r3, error
    bne r4, r0, error
    ;(2^31-1)^2
    lwu r1, 32(r0)
    addi r2, r0, 1
    lwu r3, 40(r0)
    multu r1, r1
    mflo r4
    mfhi r5
    bne r2, r4, error
    bne r3, r5, error
    syscall 0

error:
    break
    syscall 0
    
