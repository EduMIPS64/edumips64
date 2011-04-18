	.data
	min:	.word32 0x80000001 ; max negative value
	.text
	lw		r1, min(r0)
	addi	r2, r0, 3000
	sub 	r2, r1, r2	; overflow

	syscall 0
