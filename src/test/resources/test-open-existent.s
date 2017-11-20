; test-open-existent.s - opens an existing file.
; (c) 2014 Andrea Spadaccini, licensed under the GNU GPL v2 or later
                  .data
  params_sys1:    .asciiz     "src/test/resources/test-open-existent.s"
                  .word64     0x1     ; O_RDONLY

                  .text
  open:           daddi       r14, r0, params_sys1
                  daddi       r2, r0, -1
                  syscall     1
                  bne         r1, r2, end

  error:          break
  end:            syscall 0
