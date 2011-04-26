;example for all syscalls        
        .data                                           
error_op:       .asciiz "An error occurred opening file"
error_cl:       .asciiz "An error occurred closing file"
error_3:        .asciiz "An error occurred reading file"
error_4:        .asciiz "An error occurred writing file"
ok_message:     .asciiz "All done"                      
value:          .space  16                               
                                                         
params_sys1:    .asciiz	"filename.txt"                  
                .word64	0xF                              
                                                         
params_sys2:    .space 8                                
                                                         
params_sys3:    .space 8                                
ind_value_3:    .space 8                               
                .word64 16                               
                                                         
params_sys4:    .space 8                                
ind_value_4:    .space 8                               
                .word64 16                               
                                                         
params_sys5:    .space 8                                
                                                         
                .text                                    
open:           daddi       r14, r0, params_sys1        
                syscall     1                            
                daddi       r2,  r0, -1                  
                dadd        r3,  r0, r1                  
		BNE         r1,  r2, read                
		daddi       r4,  r0, error_op           
		j           end                          
                                                         
read:           daddi       r14, r0, params_sys3        
                sw          r3,  params_sys3(r0)        
                daddi       r5,  r0, value               
                sw          r5,  ind_value_3(r0)       
                syscall     3                            
                BNE         r1,  r2, write               
                daddi       r4,  r0, error_3            
                j           end                          
                                                         
write:          daddi       r14, r0, params_sys4        
                sw          r3,  params_sys4(r0)        
                daddi       r5,  r0, value               
                sw          r5,  ind_value_4(r0)       
                syscall     4                            
                BNE         r1,  r2, close                
                daddi       r4,  r0, error_4              
                j           end                          
                                                         
close:          daddi       r14, r0, params_sys2        
                sw          r3,  params_sys2(r0)        
                syscall     2                                           
               	daddi	    r4,  r0, ok_message           
		BNE 	    r1,  r2, end                  
		daddi 	    r4,  r0, error_cl             
                                                         
end:            sw          r4,  params_sys5(r0)            
                daddi       r14, r0, params_sys5             
                syscall     5                                
                syscall     0                                   
