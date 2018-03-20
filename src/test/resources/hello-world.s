; hello-world.s - test file for EduMIPS64.
;
; Prints a "Hello world" message with some interpolated strings to stdout.
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
  format_str:     .asciiz   "%dth of %s:\n%s version %i.%i is being tested! 100%% success!"
  s1:             .asciiz   "July"
  s2:             .asciiz   "EduMIPS64"
  fs_addr:        .space    4
                  .word     9    
  s1_addr:        .space    4
  s2_addr:        .space    4
                  .word     1
                  .word     2
  test:
                  .code
                  daddi     r5, r0, format_str
                  sw        r5, fs_addr(r0)
                  daddi     r2, r0, s1
                  daddi     r3, r0, s2
                  sd        r2, s1_addr(r0)
                  sd        r3, s2_addr(r0)
                  daddi     r14, r0, fs_addr
                  syscall   5
                  syscall   0
