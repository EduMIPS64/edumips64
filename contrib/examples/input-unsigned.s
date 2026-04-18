.data
ReadParams:	.word	0 
BuffAddr:	.space	8
Count:		.word	80
ReadBuffer:	.space	80
save_R2:        .space 8
save_R3:        .space 8
save_R4:        .space 8
save_R5:        .space 8
save_R6:        .space 8

.code
input_unsigned:
sd r2,save_R2(r0)
sd r3,save_R3(r0)
sd r4,save_R4(r0)
sd r5,save_R5(r0)
sd r6,save_R6(r0)

daddi	r2, r0, ReadBuffer
sd	r2, BuffAddr(r0)
daddi	r14, r0, ReadParams

syscall	3

dadd r6,r0,r1
daddi	r2, r0, 0
daddi   r5, r0, 0
daddi	r3, r0, 0
daddi	r4, r0, 10

_loop:	
lb r3, ReadBuffer(r2);
daddi r3, r3, -48
dmult r5, r4
mflo r5
dadd r5, r5, r3
daddi r2, r2, 1
bne r2,r6,_loop

dadd r1,r5,r0
ld r2,save_R2(r0)
ld r3,save_R3(r0)
ld r4,save_R4(r0)
ld r5,save_R5(r0)
ld r6,save_R6(r0)
jr r31
syscall	0
