.data
src:    .byte 0, 1, 2, 3, 4, 5, 6, 7
dst:    .space 0

.code

lb  r1, 0(r0)
lb  r2, 1(r0)
lb  r3, 2(r0)
lb  r4, 3(r0)
lb  r5, 4(r0)
lb  r6, 5(r0)
lb  r7, 6(r0)
lb  r8, 7(r0)

sb  r1, 8(r0)
sb  r2, 9(r0)
sb  r3, 10(r0)
sb  r4, 11(r0)
sb  r5, 12(r0)
sb  r6, 13(r0)
sb  r7, 14(r0)
sb  r8, 15(r0)
