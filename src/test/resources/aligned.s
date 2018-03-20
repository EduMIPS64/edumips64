; aligned.s - test file for EduMIPS64.
;
; Test containing many correctly aligned load/store instructions,
; that will test whether the alignment detection code works correctly.
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
    LB R1, 0(R0)
    LB R2, 1(R0)
    LB R3, 2(R0)
    LB R4, 3(R0)
    LB R5, 4(R0)
    LB R6, 5(R0)
    LB R7, 6(R0)
    LB R8, 7(R0)
    LB R9, 8(R0)
    LB R10, 9(R0)
    LB R11, 10(R0)

    LBU R1, 0(R0)
    LBU R2, 1(R0)
    LBU R3, 2(R0)
    LBU R4, 3(R0)
    LBU R5, 4(R0)
    LBU R6, 5(R0)
    LBU R7, 6(R0)
    LBU R8, 7(R0)
    LBU R9, 8(R0)
    LBU R10, 9(R0)
    LBU R11, 10(R0)

    SB R1, 0(R0)
    SB R2, 1(R0)
    SB R3, 2(R0)
    SB R4, 3(R0)
    SB R5, 4(R0)
    SB R6, 5(R0)
    SB R7, 6(R0)
    SB R8, 7(R0)
    SB R9, 8(R0)
    SB R10, 9(R0)
    SB R11, 10(R0)

    LH R1, 0(R0)
    LH R2, 2(R0)
    LH R3, 4(R0)
    LH R4, 8(R0)
    LH R5, 10(R0)

    LHU R1, 0(R0)
    LHU R2, 2(R0)
    LHU R3, 4(R0)
    LHU R4, 8(R0)
    LHU R5, 10(R0)

    SH R1, 0(R0)
    SH R2, 2(R0)
    SH R3, 4(R0)
    SH R4, 8(R0)
    SH R5, 10(R0)

    LW R1, 0(R0)
    LW R2, 4(R0)
    LW R3, 8(R0)
    LW R4, 12(R0)
    LW R5, 16(R0)

    LWU R1, 0(R0)
    LWU R2, 4(R0)
    LWU R3, 8(R0)
    LWU R4, 12(R0)
    LWU R5, 16(R0)

    SW R1, 0(R0)
    SW R2, 4(R0)
    SW R3, 8(R0)
    SW R4, 12(R0)
    SW R5, 16(R0)

    LD R1, 0(R0)
    LD R2, 8(R0)
    LD R3, 16(R0)

    SD R1, 0(R0)
    SD R2, 8(R0)
    SD R3, 16(R0)
