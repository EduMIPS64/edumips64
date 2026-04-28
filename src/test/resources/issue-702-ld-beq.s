; issue-702-ld-beq.s - test file for EduMIPS64.
;
; Regression test for the Load -> branch hazard (TEST5 in Prof. Patti's
; forwarding analysis).
;
; A load is immediately followed by a branch that reads the loaded value:
; the load produces the value at the end of MEM, while the branch needs
; it at the start of ID. The simulator must therefore stall the branch
; for two cycles, both with and without forwarding:
;
;   * Without forwarding: 'beq' (ID) waits for 'ld' (WB) -> 2 stalls.
;   * With forwarding:    'ld' produces in MEM, 'beq' needs it in ID,
;                         and there is no MEM->ID forwarding path, so the
;                         branch must wait for the value to be available
;                         via WB->ID one cycle later -> 2 stalls.
;
; (c) 2026 EduMIPS64 project
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
somewhere:      .space 8

        .code
        ld      r16, somewhere(r0)
        beq     r16, r0, finish
finish: nop
        syscall 0
