	.data
	a:	.word32 -16	; -16 signed or (2^32)-17 (FFFFFFF0) unsigned
	b:	.word32 3	; 3
	max:	.word32 0xFFFFFFFF	;(-1)
	uno: 	.word32 1 

	.code
	lw		r1, a(r0) 		; r1 = FFFFFFFFFFFFFFF0
	lw		r2, b(r0)		; r2 = 0000000000000003
	DIV		r1, r2			; execute division -16 / 3
	MFHI    r3				; r3 = r1 % r2 = FFFFFFFFFFFFFFFF
	MFLO    r4				; r4 = r1 / r3 = FFFFFFFFFFFFFFFB
	DIVU	r1, r2			; execute unsigned division (2^32)-17 / 3 
	MFHI    r5				; r5 = r1 % r2 = 0000000000000000 
	MFLO    r6				; r6 = r1 / r2 = 0000000055555550
	; try div by zero, use r5 
	DIV		r1, r5			; div by zero
	MFHI    r8				; UNPREDICTABLE
	MFLO    r9				; UNPREDICTABLE
	DIVU	r1, r5			; div by zero
	MFHI    r9				; UNPREDICTABLE 
	MFLO    r10				; UNPREDICTABLE
	lw		r11, max(r0)	; r11 = FFFFFFFFFFFFFFFF
	lw		r12, uno(r0)	; r12 = 1
	DIV		r11, r12		; execute div (2^32)-1 /1
	MFHI    r13				; r13 = 0000000000000000
	MFLO    r14				; r14 = FFFFFFFFFFFFFFFF
	DIVU	r11, r12		; execute div (2^32)-1 /1
	MFHI    r15				; r15 = 000000000000000
	MFLO    r16				; r16 = 00000000FFFFFFF
	DIV		r12, r11		; execute div 1 / (2^32)-1 
	MFHI    r17				; r17 = 0000000000000000
	MFLO    r18				; r18 = FFFFFFFFFFFFFFFF
	DIVU	r12, r11		; execute div 1/ (2^32)-1 
	MFHI    r19				; r19 = 0000001 
	MFLO    r20				; r20 = 0000000

	syscall 0

