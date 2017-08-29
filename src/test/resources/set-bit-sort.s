; Author: Nikos Lazaridis (Nik-Lz @GitHub)
; Date:   April, 2017
; UoA, DIT
;
; EDUMIPS64 Assembly program
; Stable sort of an array of integers in ascending order of their set bit count

.data
formattedString:	.asciiz	"%d|%d\n"			; ints[i]|counts[i] (MUST be 1st)
start_address:		.space	4					; supply this address to $14 for proper output. ***** This address MUST lie between 
												;		the formatted string and its parameters. 
												;		It should be in memory after formattedString in .data section *****
arg1:				.space	4					; space reserved for 1st parameter = ints[i]
arg2:				.space	4					; space reserved for 2nd parameter = counts[i]
ints:				.word32	5896, 32629, 40462, 25915, 43686, 32195, 15760, 26949, 56963, 26935, 4849, 39922, 48734, 45272, 39751, 43394, 23519, 23449, 35859, 36197, 60844, 52516, 10574, 45875, 2893, 11347, 44221, 57274, 37779, 55826, 34531, 35593, 15305, 55293, 50462, 54122, 23324, 36866, 24398, 44839, 61539, 56827, 47791, 35074, 28635, 50600, 9284, 60351, 24778, 48846, 37209, 26086, 20175, 64610, 26222, 20503, 7122, 39017, 38019, 39219, 14302, 3425, 52754, 17516, 50992, 58199, 8117, 49317, 16012, 63515, 46161, 48598, 32775, 20269, 2126, 44489, 64292, 52298, 23663, 37727, 56743, 49401, 42481, 19204, 56058, 47593, 52438, 62478, 37030, 28214, 31514, 41171, 4753, 15481, 18185, 64146, 42364, 7084, 32623, 63989, 11895, 26881, 25193, 25361, 25665, 31141, 1916, 10452, 17193, 23024, 50099, 24682, 53946, 41081, 20165, 60870, 48948, 48449, 30295, 36938
counts: 			.space 	480 				; array of 120 integers initialized to 0

.text
main:
		daddi $s2, $zero, ints 					; base address of ints stored in $s2 = $18
		daddi $s3, $zero, counts 				; base address of counts stored in $s3
		daddi $s5, $zero, 480					; $s5 = size = 4 * 120

		dadd $s4, $zero, $zero					; $s4 will be used for indexing the arrays. For starters $s4 = $20 = i = 0
loop:
		beq	$s4, $s5, exitLoop					; if (i == size) exit loop
		dadd $s1, $s4, $s2						; full address of element in ints stored in $s1
		dadd $s0, $s4, $s3						; full address of element in counts stored in $s0
		lw $a1, 0($s1)							; load $a1 = ints[i] from memory

;countOnes:										; "procedure" enter (inlined in main at the moment)
		dadd $t1, $a1, $zero					; safely copy $t1 = $a1 = num
		dadd $t0, $zero, $zero					; initialize $t0 = $8 = counts[i] = 0
while:
		beqz $t1, exitWhile						; if (num == 0) exit while loop 
		daddi $t2, $t1, -1						; $t2 = num - 1
		nop
		and $t1, $t1, $t2						; num = num & $t2
		daddi $t0, $t0, 1						; count++
		j while
		nop
exitWhile:
;exitCountOnes:									; "procedure" exit
		
		sw $t0, ($s0)							; store $t0 = counts[i] in memory
		daddi $s4, $s4, 4						; i++
		j loop
		nop
exitLoop:
		daddi $s4, $zero, 0						; i = 1


doubleInsertionSort:							; start sort "procedure" (inlined in main here)
		; insertion sorting based on the counts array values (set bits) (of higher priority)
		daddi $s4, $zero, 4						; i = 1
sLoop1:	
		beq $s4, $s5, loop1Exit
		dadd $s0, $s4, $s3 						; &counts[i]
		dadd $s1, $s4, $s2 						; &ints[i]
		nop
		lw $a0, ($s0)							; key1 = $a0 = counts[i]
		lw $a1, ($s1) 							; key2 = $a1 = ints[i]

		daddi $s6, $s4, -4						; $s6 = j = i - 1, offset from base of the array
		daddi $t3, $zero, -4					; temp = -1 (for comparing 1st loop condition)
inLoop1:
		beq $s6, $t3, inExit1					; inLoop1 condition1: if (j == temp) ExitLoop
		dadd $t5, $s6, $s3 						; &counts[j] = $t5
		nop
		lw $a2, ($t5)							; counts[j] = $a2
		nop
		dsub $t2, $a0, $a2 						; $t2 = key1 - counts[j]
		nop
		bgez $t2, inExit1 						; inLoop1 condition2_1: if ($t2 > 0) ExitLoop
		beqz $t2, inExit1 						; inLoop1 condition2_2: if ($t2 == 0) ExitLoop
		
		daddi $t4, $s6, 4 						; $t4 = k = j + 1
		dadd $t8, $t4, $s3 						; &counts[j+1] = $t8
		nop
		sw $a2, ($t8)							; counts[j+1] = counts[j]
		dadd $t7, $s6, $s2 						; &ints[j] = $t7
		nop
		lw $a3, ($t7)							; ints[j] = $a3
		dadd $t9, $t4, $s2 						; &ints[j+1] = $t9
		nop
		sw $a3, ($t9)							; ints[j+1] = ints[j]

		daddi $s6, $s6, -4						; j--
		j inLoop1
		nop
inExit1:
		daddi $t4, $s6, 4						; $t4 = k = j + 1
		nop
		dadd $t8, $t4, $s3 						; &counts[j+1]
		nop
		sw $a0, ($t8)							; counts[j+1] = key1
		dadd $t9, $t4, $s2						; &ints[j+1]
		nop
		sw $a1, ($t9)							; ints[j+1] = key2
		
		daddi $s4, $s4, 4						; i++
		j sLoop1
		nop
loop1Exit:
		
		; insertion sorting the values of ints given the corresponding counts value
		dadd $s4, $zero, $zero					; i = 0
sLoop2:
		beq $s4, $s5, loop2Exit
		dadd $s0, $s4, $s3 						; &counts[i]
		nop
		lw $a0, ($s0)							; counts[i] = currentCount = $a0
		dadd $s1, $s4, $s2 						; &ints[i]
		nop
		lw $a1, ($s1)							; ints[i] = value = $a1
		daddi $s6, $s4, -4 						; j = i - 1
		daddi $t3, $zero, -4					; temp = -1 (for comparing 1st loop condition)
inLoop2:
		beq $s6, $t3, inExit2 					; inLoop2 condition1: if (j == temp) ExitLoop
		dadd $t7, $s6, $s2 						; &ints[j] 
		nop
		lw $a3, ($t7) 							; ints[j]
		nop
		dsub $t2, $a1, $a3 						; $t2 = value - ints[j]
		nop
		bgez $t2, inExit2 						; inLoop2 condition2_1: if ($t2 > 0) ExitLoop
		beqz $t2, inExit2 						; inLoop2 condition2_2: if ($t2 == 0) ExitLoop
		dadd $t5, $s6, $s3 						; &counts[j]
		nop
		lw $a2, ($t5) 							; counts[j]
		nop
		bne $a0, $a2, inExit2 					; inLoop2 condition3: if (currentCount != counts[j]) ExitLoop

		daddi $t4, $s6, 4 						; $t4 = k = j + 1
		dadd $t7, $s6, $s2 						; &ints[j] = $t7
		nop
		lw $a3, ($t7)							; ints[j] = $a3
		dadd $t9, $t4, $s2 						; &ints[j+1] = &ints[k] = $t9
		nop
		sw $a3, ($t9) 							; ints[j+1] = ints[j]

		daddi $s6, $s6, -4						; j--
		j inLoop2
		nop
inExit2:
		daddi $t4, $s6, 4 						; $t4 = k = j + 1
		nop
		dadd $t9, $t4, $s2 						; &ints[j+1] = $t9
		nop
		sw $a1, ($t9) 							; ints[j+1] = value

		daddi $s4, $s4, 4 						; i++
		j sLoop2
		nop
loop2Exit:
;exitSort:										; terminate sort "procedure"


		; Memory Mapped I/O for interfacing with the terminal window
		dadd $s4, $zero, $zero					; $s4 = i = 0
		daddi $a2, $zero, formattedString		; supply the address of formatted string as argument to syscall 5
printOutput:									; "procedure" to print results from the 2 arrays
		beq	$s4, $s5, end						; condition : if (i == size) goto end
		; copying from a memory location to another (through a register)
		dadd $s1, $s4, $s2						; full address of ints[i]
		dadd $s0, $s4, $s3						; full address of counts[i]
		nop
		lw $a1, 0($s1)							; load $a1 = ints[i] from memory
		lw $a0, ($s0)							; load $a0 = counts[i] from memory
		nop
		sw $a1, arg1($zero)						; store contents of $a1 in memory to be printed
		sw $a0, arg2($zero)						; store contents of $a0 in memory to be printed
		; copying ended, now to print..
		daddi $14, $zero, start_address			; String will be printed using syscall 5. Its argument must be the starting 
												; 		address of the (formatted) string supplied by $14 = $t6
		syscall 5								; prints ouput (on the MEM stage)
		daddi $s4, $s4, 4						; i++
		j printOutput
		nop
end:
		
		syscall 0								; terminate execution

