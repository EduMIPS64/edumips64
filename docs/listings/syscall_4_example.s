; Example for SYSCALL 4
                .data
params_sys4:    .space      8         
ind_value:      .space      8         
                .word64     16      
error_4:        .asciiz     "Errore durante la scrittura su file"   
ok_message:     .asciiz     "Tutto a posto"   
value:          .space      30                        


                .text
                #include    print.s   

read:           daddi       r14, r0, params_sys4
                sw          $s2, params_sys3_4(r0)
                daddi       $s1, r0, value   
                sw          $s1, ind_value(r0)   
                syscall     3         
                daddi       $s0, r0, -1         
                BNE         r1, $s0, print_string
                daddi       $a0, r0, error_4
                jal         print_string            
