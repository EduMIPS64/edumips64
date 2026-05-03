; mul.d-loop-waw.s
;
; Reproduces the bug where, on each iteration of the loop, the
; *same* mul.d InstructionInterface object is pushed through the
; multiplier pipeline. While iteration N's mul.d is progressing
; through M5 / M6 / M7, iteration N+1's mul.d sits WAW-stalled in
; ID (the previous mul.d will eventually write f1). Both pipeline
; slots reference the same Java object — so the per-instruction
; cycle-builder tag must not be blindly applied to the multiplier
; slot, otherwise it would inherit the ID slot's "WAW" tag.

.code

daddi r10, r0, 5
loop:

daddi r10, r10, -1
mul.d   f1, f0, f0

bne r10, r0, loop
syscall 0
