; negative-offsets.s - test file for EduMIPS64.
;
; Test containing load/store instructions with negative offsets.
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
        ; Store 8 in memory location 0.
        ADDI r1, r0, 8
        SW r1, -8(r1)
        LW r2, -8(r1)
        BNE r1, r2, error

        ; store 32767 in memory location 0.
        ADDI r3, r0, 32767
        SW r3, -32767(r3)
        LW r4, -32767(r3)
        BNE r3, r4, error
        SYSCALL 0

error:  BREAK
        SYSCALL 0
