	.data
	min:	.word32 0xB0000001 ; big negative value
	.text
	addi	r1, r0, 16		;0000 0000 0000 0000 0000 0000 0001 0000 bin	16  dec
	addi	r2, r0, -16		;1111 1111 1111 1111 1111 1111 1111 0000 bin	-16 dec
	addi 	r3, r0, 2
	sll 	r5, r1, 2		;0000 0000 0000 0000 0000 0000 0100 0000 bin	64  dec
	sllv 	r6, r1, r3		;0000 0000 0000 0000 0000 0000 0100 0000 bin	64  dec
	sll 	r8, r2, 2		;1111 1111 1111 1111 1111 1111 1100 0000 bin	-64 dec
	sllv 	r9, r2, r3		;1111 1111 1111 1111 1111 1111 1100 0000 bin	-64 dec
	lw		r11, min(r0)	;1011 0000 0000 0000 0000 0000 0000 0001 bin	-1342177279 dec
	sll 	r13, r11, 2		;1100 0000 0000 0000 0000 0000 0000 0100 bin	-1073741820
	dsll	r14, r11, 2		;the last 32bits must be equal to sll result
	sllv 	r15, r11, r3	;1100 0000 0000 0000 0000 0000 0000 0100 bin	-1073741820
	dsllv 	r16, r11, r3	;;the last 32bits must be equal to sll result
	
	;sll	r11, r1, -2		;Wrong value
	
	syscall 0