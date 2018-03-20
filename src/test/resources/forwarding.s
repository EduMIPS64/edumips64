; forwarding.s - test file for EduMIPS64.
;
; Test file contaning instructions with data dependencies whose stalls can be
; shortened through forwarding.
;
; The expected number of execution cycles are:
; * without forwarding: 19
; * with forwarding: 16
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
    dadd    r1, r2, r3
    dsub    r4, r1, r5
    nop
    nop
    nop
    nop
    nop
    lw      r1, 0(r0)
    dsub    r2, r1, r5
    syscall 0
