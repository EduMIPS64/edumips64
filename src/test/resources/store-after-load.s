; store-after-load.s - test file for EduMIPS64.
;
; Functional check for load/store instructions
;
; Checks whether the data dependency in a load/store cycle is honored.
; Executes a break instruction if the test fails
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
        .data
src:    .byte 10
dst:    .byte 0

        .code
        lb      r1, src(r0)
        sb      r1, dst(r0)
        addi    r2, r0, 10
        lb      r3, dst(r0)
        dsub    r4, r3, r2
        bnez    r4, fail
        syscall 0

fail:   break
