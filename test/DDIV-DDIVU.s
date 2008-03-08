.data
a:	.word64 -16	; -16 signed or (2^64)-17 (FFFFFFFFFFFFFFF0) unsigned
c:	.word64 3	; 3
max:	.word64 0xFFFFFFFFFFFFFFFF
uno: 	.word64 1 

.code
ld	r1,a(r0) 	; r1 = FFFFFFFFFFFFFFF0
ld	r2,c(r0)	; r2 = 0000000000000003
DDIV	r1,r2		; execute division -16 / 3
MFHI    r3		; r3 = r1 % r2 = FFFFFFFFFFFFFFFF
MFLO    r4		; r4 = r1 / r3 = FFFFFFFFFFFFFFFB
DDIVU   r1,r2		; execute unsigned division (2^64)-17 / 3 
MFHI    r5		; r5 = r1 % r2 = 0000000000000000 
MFLO    r6		; r6 = r1 / r2 = 5555555555555550
; try div by zero, use r5 
DDIV    r1,r5		; div by zero
MFHI    r8		; UNPREDICTABLE
MFLO    r9		; UNPREDICTABLE
DDIVU   r1,r5		; div by zero
MFHI    r9		; UNPREDICTABLE 
MFLO    r10		; UNPREDICTABLE
ld	r11, max(r0)	; r11 = FFFFFFFFFFFFFFFF
ld	r12, uno(r0)	; r12 = 1
DDIV   r11,r12		; execute div (2^64)-1 /1
MFHI    r13		; r13 = 0000000000000000
MFLO    r14		; r14 = FFFFFFFFFFFFFFFF
DDIVU   r11,r12		; execute div (2^64)-1 /1
MFHI    r15		; r15 = 000000000000000 
MFLO    r16		; r16 = FFFFFFFFFFFFFFF
DDIV   r12,r11		; execute div 1 / (2^64)-1 
MFHI    r17		; r17 = 000000000000001
MFLO    r18		; r18 = 000000000000000
DDIVU  r12,r11		; execute div 1/ (2^64)-1 
MFHI    r19		; r19 = 000000000000001 
MFLO    r20		; r20 = 000000000000000
syscall 0

