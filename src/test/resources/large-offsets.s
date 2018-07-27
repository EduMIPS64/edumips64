; large-offsets.s - test file for EduMIPS64.
;
; Test containing load/store instructions with large offsets.
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
    LB r1, 16384(r0)
    LB r1, 32760(r0)
    LB r1, 32768(r0)
    LD r1, 16384(r0)
    LD r1, 32760(r0)
    LD r1, 32768(r0)
    SB r1, 16384(r0)
    SB r1, 32760(r0)
    SB r1, 32768(r0)
    SD r1, 16384(r0)
    SD r1, 32760(r0)
    SD r1, 32768(r0)
    SYSCALL 0
