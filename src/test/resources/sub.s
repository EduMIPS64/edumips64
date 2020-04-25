; sub.s - test file for EduMIPS64.
;
; Tests sub instruction and checks the result.
;
; (c) 2018 Oscar Elhanafey and the EduMIPS64 team
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
.code

        addi r1, r0, 20
        addi r2, r0, 25
        addi r3, r0, 5
        addi r4, r0, -5

sub:    sub  r5, r2, r1
        bne  r5, r3, error

        sub  r5, r1, r2
        bne  r5, r4, error

equal:  sub  r5, r1, r1
        bne  r5, r0, error
        syscall 0

error:  break
        syscall 0
