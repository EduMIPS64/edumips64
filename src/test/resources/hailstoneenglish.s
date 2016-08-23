    ; An EduMIPS64 test program kindly donated by Gerardo Puga in Issue #132.
    ; https://github.com/lupino3/edumips64/issues/132
    ; Adapted by Andrea Spadaccini by removing all compilation warnings.
    
	; ARQUITECTURA DE COMPUTADORES II, 2014
	; TP 02: Segmented Architectures
	;
	; Calculates the maximum value reached by the Hailstone sequences of the firsts 100 numbers.
	;

	; Start of the data segment
	; ---

	.data

	; Results table
result: .word32 0

	; -----------------

	; Start of the program segment
	; ---

	.text
	
	; Initialize the main loop index
	daddi R5,R0,1

	; Load the start address of the results table on R4
	daddi R4,R0,result

numloop: 
        ; Initialize R2 with the first number of the sequence
	dadd R2,R0,R5

	; ...this first number is currently also the current maximum value. This maximum is stored in R3.
	dadd R3,R0,R2

	; -----------------

	; Start of the loop that calculates the Hailstone sequence
hailloop: 

	; Is the current number even or odd?
	andi R1,R2,1
	bne  R1,r0,odd   ; if odd, then go to "odd"

	; -----------------
	
	; Even numbers
even:
	; Divide by two
	dsrl R2,R2,1

	; Skip the code for odd numbers
	j anynumber

	; -----------------

	; Odd numbers
odd:
	; Multiply by three and add one
	dsll R1,R2,1
	dadd R2,R1,R2
	daddi R2,R2,1

	; -----------------
anynumber:
	
	; If the new number is higher than the maximum, this is the new maximum
	dsub R1,R3,R2      ; Calculate the difference between the current sequence number and the max, put it in R1
	dsrl R1,R1,31      ; Remove all bits, but the one that contains the sign of the result
	beq R1,r0,skipnewmax ; If the sign bit is zero, the current sequence number is not higher than the current maximum

	dadd R3,R0,R2	   ; Replace the current maximum with the current sequence number
skipnewmax:

	; If the current sequence number is 1, then we reached the end of the sequence
	daddi R1,R2,-1
	bne R1,r0,hailloop

	; Store the final maximum on the results table
	sw R3,0(R4)

	; Increment the table index
	daddi R4,R4,4 

	; Increment the current main loop index
	daddi R5,R5,1

	; check if we have already covered the first 100 natural numbers
	daddi R1,R5,-101
	bne R1,r0,numloop

	halt


