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
