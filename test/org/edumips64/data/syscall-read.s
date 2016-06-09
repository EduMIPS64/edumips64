; Test SYSCALL - Open and read.

                  .data 
                  ; Expected contents of the file.
  expected:       .asciiz     "This is a test.."
                  ; File to open, in read-only (0x1) mode.
  params_sys1:    .asciiz     "test/org/edumips64/data/test.txt"
                  .word64     0x1

  params_sys3:    .space 8
  ind_value:      .space 8
                  ; Gonna read 16 bytes.
  to_read:        .word64 16
                  ; Actual data read from file.
  actual:         .space      16                    

; ---------------------------------------------------------------

                  .text
                  ; Store -1 in $s0, to compare it later with the
                  ; return value of syscalls to check for errors.
                  daddi       $s0, $zero, -1

                  ; -- Open the file (SYSCALL 1)
                  daddi       $t6, $zero, params_sys1    
                  syscall     1    
                  beq         $at, $s0, error            

                  ; -- Read the data (SYSCALL 3)
                  sw          $at, params_sys3($zero)
                  daddi       $s1, $zero, actual
                  sw          $s1, ind_value($zero)
                  daddi       $t6, $zero, params_sys3
                  syscall     3
                  beq         $at, $s0, error            

                  ; -- Compare the data read from file with the
                  ;    expected data.
                  ld          $s1, expected($zero)
                  ld          $s2, actual($zero)
                  bne         $s1, $s2, error
                  daddi       $s0, $zero, 8     ; offset
                  ld          $s1, expected($s0)
                  ld          $s2, actual($s0)
                  bne         $s1, $s2, error

  end:            syscall 0

  error:          break
                  syscall 0
