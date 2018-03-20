; test-strlen.s - test file for EduMIPS64.
;
; Tests for the strlen routine.
; Executes a break instruction if the test fails
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
; Parameter for strlen
strlen_params:  .space 8

edu_str:        .asciiz "EduMIPS64"
empty_string:   .space 8

; Data for the error reporting routine
test_failed:    .asciiz "Test failed"
error_params:   .byte       2       ; stderr
error_addr:     .word64     0
                .byte       11

                .code

; Test 1: compute the length of an empty string. If it is different from 0,
;         fail

    daddi       r1, r0, empty_string
    sd          r1, strlen_params(r0)
    daddi       r14, r0, strlen_params
    jal         strlen
    bne         r1, r0, error

; Test 2: compute the length of the "EduMIPS64" string. If it is different
;         from 9, fail.

    daddi       r1, r0, edu_str
    sd          r1, strlen_params(r0)
    daddi       r14, r0, strlen_params
    jal         strlen
    daddi       r1, r1, -9
    bne         r1, r0, error

    syscall     0

error:
    daddi       r1, r0, test_failed
    sd          r1, error_addr(r0)
    daddi       r14, r0, error_params
    syscall     4 

; Execute a break instruction, so that the unit test fails
    break 
    
#include utils/strlen.s;
