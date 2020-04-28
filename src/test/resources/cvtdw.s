; cvtdw.s - test file for EduMIPS64.
;
; Executes cvt.d.w and ensures it converts to double.
;
; (c) 2020 Oscar Elhanafey and the EduMIPS64 team
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
        .double 10

.code
        ldc1 f2, 0(r0)

        addi r1, r0, 10
        mtc1 r1, f1
        cvt.d.w f1, f1

        c.eq.d 7, f1, f2 ; will be false if does not convert to double
        bc1f 7, error
        syscall 0


error:
        break
        syscall 0
