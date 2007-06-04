;Example far all syscalls
	.data
error_op:	.asciiz "An error occurred opening file"	;error message
error_cl:	.asciiz "An error occurred closing file"	;error message
error_3:	.asciiz "An error occurred reading file"	;error message
error_4:	.asciiz "An error occurred writing file"	;error message
ok_message:	.asciiz "All right, operation succesfully done"	;ok message
value:		.space 30					;filename.txt value

params_sys1:	.asciiz	"/home/giorgio/Desktop/filename.txt"	;filename path
		.word64	0xF					;opening file in both reading and writing mode,with append and creation options(3+4+8=15)

params_sys2:	.space 8					;file descriptor

params_sys3_4:	.space 8					;file descriptor		
ind_value:	.space 8					;memory address of the label 'value'
		.word64 16					;number of bytes to read

                .text  

open:           daddi       r14, r0, params_sys1    
                syscall     1    
                daddi       $s0, r0, -1
                dadd        $s2, r0, r1		
		BNE 	    r1,$s0,read			
		daddi 	    $a0,r0,error_op
		j 	    end

read:           daddi       r14, r0, params_sys3_4    
                sw          $s2, params_sys3_4(r0)
                daddi       $s1, r0, value            
                sw          $s1, ind_value(r0)            
                syscall     3                 			
		BNE 	    r1,$s0,write			
		daddi 	    $a0,r0,error_3
		j 	    end

write:
		daddi 	    r14,r0,params_sys3_4		
		sw	    $s2,params_sys3_4(r0)		
		daddi 	    $s1,r0,value			
		sw 	    $s1,ind_value(r0)			
		syscall     4					
		BNE 	    r1,$s0,close			
		daddi 	    $a0,r0,error_4
		j 	    end

close:          daddi       r14, r0, params_sys2        
                sw          $s2, params_sys2(r0)    
                syscall     2            
                daddi       $s0, r0, -1        
               	daddi	    $a0,r0,ok_message			
		BNE 	    r1,$s0,end			
		daddi 	    $a0,r0,error_cl

end:		jal 	    print_string
		syscall 0
		
		#include    print.s 
