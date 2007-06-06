; Example for SYSCALL 3
                .data
params_sys3:    .space      8                
ind_value:      .space      8            
                .word64     16        
error_3:        .asciiz     "Errore durante la lettura del file"    
ok_message:     .asciiz     "Tutto a posto"    

value:          .space 30                    

                .text 

read:           daddi       r14, r0, params_sys3    
                sw          $s2, params_sys3(r0)
                daddi       $s1, r0, value            
                sw          $s1, ind_value(r0)            
                syscall     3            
                daddi       $s0, r0, -1            
		daddi	    $a0,r0,ok_message			
		BNE 	    r1,$s0,end			
		daddi 	    $a0,r0,error_3

end:		jal 	    print_string
		syscall 0
		
		#include    print.s 
