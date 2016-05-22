; Test for the Dinero tracefile generation code.
;
; Will produce a tracefile with 3 instruction accesses and 1 store.
.data
dst: .word 0

.code
addi r1,r0,42
sd r1,dst(r0)
syscall 0
