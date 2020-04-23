; sll.s - test file for EduMIPS64.
;
; Tests sll instruction and ensures that it left shifts correctly.
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
.code

        addi r1, r0, 11
        addi r2, r0, 22
        addi r3, r0, 352

shift:  sll r4, r1, 1
        bne  r4, r2, error

        sll r4, r1, 5
        bne  r4, r3, error
        syscall 0

error:  break
        syscall 0
