; bgez.s - test file for EduMIPS64.
;
; Executes bgez and ensures it branches correctly.
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
        addi r1, r0, 2
        addi r2, r0, -2

greater:
        bgez r1, zero ; should branch 2 >= 0
        break
        syscall 0

zero:
        bgez r0, less ; should branch 0 >= 0
        break
        syscall 0

less:
        bgez r2, error ; should not branch -2 < 0
        syscall 0

error:
        break
        syscall 0
