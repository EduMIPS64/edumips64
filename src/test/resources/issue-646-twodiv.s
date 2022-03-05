;--------------------------------------------------------------------------
; Two divisions at once
;--------------------------------------------------------------------------
;--------------------------------------------------------------------------
; DATA SPACE
;--------------------------------------------------------------------------
       								
              .data           
								
A:            .double  5.2     ; data value for A.
B:            .double  -0.001  ; data value for B.
C:            .double  4.75    ; data value for C.
D:            .double  -2.1    ; data value for D.


;--------------------------------------------------------------------------
; CODE SPACE
;--------------------------------------------------------------------------
               .text        ; start of code space

               ; Usage of GENERAL PURPOSE REGISTERS (GPRs)

main:          

               l.d f1, A(r0)       ; load value of A 
               l.d f2, D(r0)       ; load value of D
               l.d f3, C(r0)       ; load value of C
               l.d f4, B(r0)       ; load value of B  
               div.d f5, f1, f2    ; two divisions at once
               div.d f4, f3, f4    ;
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop 
               nop
               nop
               nop
               nop
               nop 
               nop
               nop 
               nop
               nop
               nop 
               nop
               nop 
               nop

             
over:          ; Stop execution, using the system call syscall 0.
               syscall 0                      