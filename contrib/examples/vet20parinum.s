; The program reads a vector of 10 positive doubles and a positive double number,
; scans the vector, and if the current element is less
; than 20 or divisible by 2, it squares it; otherwise it subtracts the given number.


;by Simona Ullo, Massimo Trubia (FPU modification)

	.data
vet:	.double 	3.0,4.0,25.0,2.0,40.0,8.0,9.0,12.0,24.0
num:	.double 	1.0
twenty:	.double	20.0
two: .double 2.0
zero: .double 0.0


	.code
	DADDI R1,R0,vet 	;R1=vet - address of variable vet
	DADDI R2,R0,num		;R2=num - address of variable num
	DSUB R3,R2,R1		;R3=num-vet - size of vet
	DADDI R4,R0,0		;R4=0 - index of vet
	LDC1 f5,twenty(R0)		;R5=Mem[twenty] - 20 (comparison value)
	LDC1 f6,num(R0)		;R6=Mem[num] - value to subtract
    LDC1 F20,two(R0)
    LDC1 F21,zero(R0)

loop:	LDC1 f7,vet(R4)		;f7=Mem[vet+R4] - load an element of the vector
	c.lt.d 0,f7,f5		;if(f7<20) FCSR[0]=1
    nop 
	BC1T 0,quad  		;... then square the vector element
	DIV.D f8,F7,F20		;check if the vector element is even
    ;e.g. 5.0/2.0=2.5   trunc(2.5)=2   2.5-trunc(2.5)=0.5   0.5>0 ==>odd number    
    CVT.L.D F10,f8      ;remove the decimal part (set rounding mode towards zero)
    CVT.D.L F9,F10      ;move the truncated value to another register
    SUB.D f11,f8,f9     ;store the remainder of the division in f11
    C.LT.D 0,f21,f11  ; if(remainder>0) FCSR[0]=1
    NOP 
	BC1F 0,quad		;... in that case, square it
	SUB.D f7,f7,f6		;f7=f7-f6 subtract num from the vector element
	ADD.D f29,f7,f21	;store the value in f29 to write it to memory
	J store_result;	    	;jump to store the modified element in memory

quad:	ADD.D f30,f21,f7	;first function parameter - value to be squared
	JAL square		;call the square function

store_result:	SDC1 F29,vet(R4)		;Mem[vet+R4]=R7 store the modified value
	DADDI R4,R4,8		;R4++
	SLT R8,R4,R3		;if(R4<R3) R8=1 
	BNEZ R8,loop		;if there are still elements in the vector, repeat the loop
	HALT
		

;SQUARE FUNCTION

square: 
	DADDI R28,R0,0		;R28=0 - loop index
	ADD.D f29,f21,f21		;R29=R30
inner_loop:
    ;f28=convert_double(r28)
    DMTC1 R28,f28       ;f28=r28
    CVT.D.L f31,f28     ;f31=convert_double(f28)
    MOV.D f28,f31       ;f28=f31
    
	C.LT.D 0,f28,f30		;if(f28<f29) FCSR[0]=1
	BC1F 0,ret		    ;return to caller
	ADD.D F29,F29,F30	;f29+=f30   add f29 to itself
	DADDI R28,R28,1		;R28++   increment loop index
	J inner_loop			    ;loop again
ret:	JR R31			;return

