; jump-table.s - test file for EduMIPS64.
;
; Verifies that a forward code label can be used as the value of a .word64
; directive (issue #1643), enabling jump tables. The program loads the address
; of a code label stored in the data section and performs an indirect jump to
; it. If the address is resolved correctly, execution reaches the target and
; halts (success); otherwise it falls through to BREAK (failure).
;
; (c) 2026 Andrea Spadaccini and the EduMIPS64 team
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
target_addr:    .word64 done        ; stores the address of the "done" code label
                .code
                ld   r1, target_addr(r0)   ; r1 = address of "done"
                jr   r1                     ; indirect jump to "done"
                break                       ; must be skipped if the jump is correct
done:
                syscall 0
