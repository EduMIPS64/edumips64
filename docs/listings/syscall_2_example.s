; Example for SYSCALL 2

                .data
params_sys2:    .space 8

error_cl:       .asciiz     "Errore durante la chiusura del file"

ok_message:     .asciiz     "Tutto a posto"

                .text
                #include    print.s    
close:          daddi       r14, r0, params_sys2        
                sw          $s2, params_sys2(r0)    
                syscall     2            
                daddi       $s0, r0, -1        
                daddi       $a0, r0, ok_message        
                bne         r1, $s0, print_string        
                daddi       $a0, r0, error_cl    
                jal         print_string            
