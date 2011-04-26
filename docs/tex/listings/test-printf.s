                .data
format_str:     .asciiz   "%d %s:\n%s versione %i.%i funziona!"
s1:             .asciiz   "Giugno"
s2:             .asciiz   "EduMIPS64"
fs_addr:        .space    4
                .word     5    
s1_addr:        .space    4
s2_addr:        .space    4
                .word     0
                .word     5
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
