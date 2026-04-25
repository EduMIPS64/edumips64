; branch-raw.s - test file for EduMIPS64.
;
; Regression test for the "Missing RAW for some branches" issue:
; the result of an ALU instruction is needed by a branch in ID,
; which must stall with forwarding enabled (the ID of the branch
; happens in the same cycle as the EX of the ALU instruction, so
; the register value is not yet available from forwarding).
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.

    .code
    slt   r1, r2, r4
    beqz  r1, finish
finish: syscall 0
