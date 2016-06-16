; Test for the Dinero tracefile generation code.
;
; Will produce a tracefile with 3 instruction accesses, 1 load and 1 store.
.data
src: .word 42
dst: .word 0

.code
ld r1,src(r0)
sd r1,dst(r0)
syscall 0
