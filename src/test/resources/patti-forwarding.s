; patti-forwarding.s - test file for EduMIPS64.
;
; Comprehensive forwarding test based on Prof. Patti's analysis.
; Tests all major RAW hazard scenarios with and without forwarding.
;
; TEST1: RAW hazard on anticipated branch (ID stage)
;   - No fwd: 'bne' (ID) waits for 'daddi' (WB) -> 2 stalls per iteration
;   - Fwd:    'bne' (ID) waits for 'daddi' (EX) to finish -> 1 stall per iteration
;   Loop runs 10 times -> 20 RAW stalls (no fwd), 10 RAW stalls (fwd)
;
; TEST2: RAW ALU/SW
;   - No fwd: 'sw' (ID) waits for 'daddi' (WB) -> 2 stalls
;   - Fwd:    'sw' needs data in MEM, 'daddi' produces in EX -> 0 stalls
;
; TEST3: RAW ALU,ALU
;   - No fwd: 'dadd' (ID) waits for 'daddi' (WB) -> 2 stalls
;   - Fwd:    direct forwarding EX/MEM to EX -> 0 stalls
;
; TEST4: RAW LD/ALU (Load-Use Hazard)
;   - No fwd: 'dadd' (ID) waits for 'ld' (WB) -> 2 stalls
;   - Fwd:    'ld' gets data in MEM, 'dadd' needs it in EX -> 1 stall
;
; (c) 2024 EduMIPS64 project
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


   ; just to run for longer, create a for loop that iterates.
   ; watch how r10 changes value in the Registers section.
   daddi   r10, r0, 10
   daddi   r20, r0, 20
   daddi   r30, r0, 30
loop:
  
   ; TEST1: RAW hazard on anticipated branch (ID stage)
   daddi   r10, r10, -1
   bne     r10, r0, loop


   ; TEST2: RAW ALU/SW
   daddi   r5, r0, 5
   sw      r5, somewhere(r0)


   ; TEST3: RAW ALU,ALU
   daddi   r11, r0, 11
   dadd    r12, r10, r11


   ; TEST4: RAW LD/ALU (Load-Use Hazard)
   ld      r15, somewhere(r0)
   dadd    r13, r15, r0


   ; Exit the program.
   syscall 0
