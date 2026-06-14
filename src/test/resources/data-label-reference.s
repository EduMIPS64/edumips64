; data-label-reference.s - test file for EduMIPS64.
;
; Verifies that a label used as the value of a .word64 directive stores the
; memory address the label points to (issue #1643). The program loads the
; stored address and compares it against the known address of the label.
; It halts (success) if they match, and executes BREAK (failure) otherwise.
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
pad:            .word64 0           ; occupies address 0
buffer:         .space  8           ; located at address 8
buf_addr:       .word64 buffer      ; stores the address of "buffer" (= 8)
                .code
                ld   r1, buf_addr(r0)   ; r1 = stored address of buffer
                daddi r2, r0, 8         ; r2 = expected address of buffer
                bne  r1, r2, fail
                syscall 0
fail:
                break
