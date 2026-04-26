; issue-702-slt-beqz.s - test file for EduMIPS64.
;
; Test case from issue #702: an ALU instruction immediately followed by a
; branch that reads its result. Branches resolve in the ID stage, but no
; EX -> ID forwarding path exists, so even with forwarding enabled there
; must be exactly one RAW stall between `slt` (writes r1 in EX) and
; `beqz r1` (reads r1 in ID).
;
; Expected RAW stalls:
;   * with forwarding:    1
;   * without forwarding: 2
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

        .code
        slt     r1, r2, r4
        beqz    r1, finish
finish: nop
        syscall 0
