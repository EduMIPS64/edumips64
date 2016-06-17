	.data
	min:	.word32 0xB0000001 ; big negative value
	.text
	addi	r1, r0, 16		;0000 0000 0000 0000 0000 0000 0001 0000 bin	16  dec
	addi	r2, r0, -16		;1111 1111 1111 1111 1111 1111 1111 0000 bin	-16 dec
	addi 	r3, r0, 2
	sra 	r5, r1, 2		;0000 0000 0000 0000 0000 0000 0000 0100 bin	4  dec
	srav 	r6, r1, r3		;0000 0000 0000 0000 0000 0000 0000 0100 bin	4  dec
	sra 	r8, r2, 2		;1111 1111 1111 1111 1111 1111 1111 1100 bin	-4 dec
	srav 	r9, r2, r3		;1111 1111 1111 1111 1111 1111 1111 1100 bin	-4 dec
	lw		r11, min(r0)	;1011 0000 0000 0000 0000 0000 0000 0001 bin	-1342177279 dec
	sra 	r13, r11, 2		;1110 1100 0000 0000 0000 0000 0000 0001 bin	-335544320 dec
	dsra	r14, r11, 2		;the last 32bits must be equal to sll result
	srav 	r15, r11, r3	;1110 1100 0000 0000 0000 0000 0000 0001 bin	-335544320 dec
	dsrav 	r16, r11, r3	;;the last 32bits must be equal to sll result

	;sra	r11, r1, -2	;Wrong value
	
	syscall 0