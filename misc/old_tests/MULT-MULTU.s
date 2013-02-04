	.data
	menouno:	.word32 0xFFFFFFFF ; -1 signed or 2^32-1 unsigned
	uno:		.word32 0x1
	high:		.word32 0x7FFFFFFF ; 2^32-1 (2147483647)
	.code
	lw		r1, menouno(r0)	; r1  = FFFFFFFFFFFFFFFF
	lw		r2, uno(r0)		; r2  = 0000000000000001
	MULT	r1, r1 			; execute -1 * -1 (1)
	MFHI    r3				; r3  = 0000000000000000
	MFLO    r4				; r4  = 0000000000000001
	MULTU	r1, r1			; execute 0xFFFFFFFF * 0xFFFFFFFF (2^32-1*2^32-1) = FFFFFFFE00000001 or 18446744065119617025
	MFHI    r5				; r5  = 00000000FFFFFFFE
	MFLO    r6				; r6  = 0000000000000001
	MULT	r2, r1			; execute -1 * 1 (-1)
	MFHI    r7				; r7  = FFFFFFFFFFFFFFFF
	MFLO    r8				; r8  = FFFFFFFFFFFFFFFF
	MULTU	r1, r2			; execute 2^32-1 * 1 (2^32-1 or 00000000FFFFFFFF) 
	MFHI    r9				; r9  = 0000000000000000
	MFLO    r10				; r10 = 00000000FFFFFFFF
	lw		r11, high(r0)	; r11 = 000000007FFFFFFF
	MULT	r11, r11		; execute 2^31-1 * 2^31-1 (3FFFFFFF00000001 or 4611686014132420609)
	MFHI    r12				; r12 = 000000003FFFFFFF
	MFLO    r13				; r13 = 0000000000000001
	syscall 0
