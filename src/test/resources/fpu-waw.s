; fpu-waw.s - test file for EduMIPS64.
;
; Test file for write-after-write FPU stalls.
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
var1: .double 3.5
var2: .double 4.5
.text
ldc1 f2,var1(r0)
ldc1 f3,var2(r0)
mul.d f4,f3,f2
add.d f4,f3,f2
halt
