; bc1f.s - test file for EduMIPS64.
;
; Executes bc1f and ensures it branches correctly.
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
        .double 3.14
        .double 2.71

.code
        ldc1 f1, 0(r0)
        ldc1 f2, 8(r0)
        ldc1 f3, 0(r0)

        c.eq.d 7, f1, f2 ; should be false
        bc1f 7, continue
        break

continue:
        c.eq.d 7, f1, f3 ; should be true
        bc1f 7, error
        syscall 0


error:
        break
        syscall 0
