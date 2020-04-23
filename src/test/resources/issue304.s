; issue304.s - test file for EduMIPS64.
;
; Regression test for issue 308.
;
; Derived from the code provided by GitHub user @jcarletta in the same issue.
;
; (c) 2020 Andrea Spadaccini and the EduMIPS64 team
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
    DADDI r2, r0, 1
    ADD.D f1, f1, f1
    ADD.D f2, f2, f2
    NOP
    DADDI r1, r0, 1
    ADD.D f3, f2 ,f2
    BNE r2, r1, error       ; In issue 304, this instruction is permanently in RAW because the second DADDI is mistakenly removed from the pipeline before it unlocks the read semaphore.
    SYSCALL 0

error:
    BREAK
