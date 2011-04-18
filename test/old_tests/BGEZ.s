.data
neg:		.word64 0xFFFFFFFFFFFFFFFF
pos:		.word64 0x7FFFFFFFFFFFFFFF
zero:		.word64	0

.code
start:		ld 	r1,neg(r0)	; r1 = FFFFFFFFFFFFFFFF 
		ld 	r2,pos(r0)	; r2 = 7FFFFFFFFFFFFFFF
		ld	r3,zero(r0)	; r3 = 0000000000000000
		bgez	r1,start	; don't jump to start
		bgez	r2,noexit	; jump to noexit
quit:		SYSCALL 0		; exit
noexit:		bgez	r3,quit		; jump to quit
		b	start		; goto start, not execute
