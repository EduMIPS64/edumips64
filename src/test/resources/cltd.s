; bc1f.s - test file for EduMIPS64.
;
; Executes c.lt.d and ensures it compares correctly.
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

less:
        c.lt.d 2, f2, f1 ; should be true 2.71 < 3.14
        bc1f 2, error

greater:
        c.lt.d 7, f1, f2 ; should be false 3.14 >= 2.71
        bc1t 7, error

equal:
        c.lt.d 3, f1, f3 ; should be false 3.14 >= 3.14
        bc1t 3, error
        syscall 0



error:
        break
        syscall 0
