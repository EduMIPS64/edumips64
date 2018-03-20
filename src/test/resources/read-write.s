; read-write.s - test file for EduMIPS64.
;
; Writes data to a file, reads it back and checks that the data is the same.
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
  params_open:    .asciiz     "syscall-testfile.txt"
         mode:    .word64     0x6   ; write/truncate.

 params_close:    .space      8     ; file descriptor

 params_write:    .space      8     ; file descriptor
   write_addr:    .space      8     ; to be populated.
                  .word64     8     ; write 8 bytes.

  params_read:    .space      8     ; file descriptor
    read_addr:    .space      8     ; to be populated.
                  .word64     8     ; read 8 bytes.
   

         data:    .ascii      "ABCDEFGH"
    read_data:    .space      8

.code
                  ; Store -1 in $s0, to compare it later with the
                  ; return value of syscalls to check for errors.
                  daddi       $s0, $zero, -1

                  ; Open file in O_WRITE / O_TRUNC.
                  daddi       $t6, $zero, params_open    
                  syscall     1    
                  beq         $at, $s0, error            

                  ; Store the file descriptor for write() and close()
                  sw          $at, params_write($zero)
                  sw          $at, params_close($zero)

                  ; Write data there.
                  daddi       $t6, $zero, params_write    
                  daddi       $s1, $zero, data
                  sw          $s1, write_addr($zero)
                  syscall     4
                  beq         $at, $s0, error            

                  ; Close file.
                  daddi       $t6, $zero, params_close    
                  syscall     2
                  beq         $at, $s0, error            

                  ; Open file in O_RDONLY
                  daddi       $s2, $zero, 1
                  sw          $s2, mode($zero)
                  daddi       $t6, $zero, params_open    
                  syscall     1    
                  beq         $at, $s0, error            

                  ; Store the file descriptor for read() and close()
                  sw          $at, params_read($zero)
                  sw          $at, params_close($zero)

                  ; Read data from it.
                  daddi       $t6, $zero, params_read    
                  daddi       $s1, $zero, read_data
                  sw          $s1, read_addr($zero)
                  syscall     3
                  beq         $at, $s0, error            

                  ; Close file.
                  daddi       $s1, $zero, 4
                  sw          $s1, params_close($zero)
                  syscall     2
                  beq         $at, $s0, error            

                  ; Compare data.
                  ld          $s1, data($zero)
                  ld          $s2, read_data($zero)
                  bne         $s1, $s2, error

  end:            syscall 0

  error:          break
                  syscall 0
