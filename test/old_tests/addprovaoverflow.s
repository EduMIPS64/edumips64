	.data
	high:		.word32 0x7FFFFFFF ; 2^32-1 (2147483647)
	.text
	lw		r1,high(r0)	; r1 = 000000007FFFFFFF	
	addi	r2, r0, 1
	add		r3, r1, r2		; overflow
	syscall 0
