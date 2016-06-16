; Test for the Dinero tracefile generation code.
;
; Will produce a tracefile with 2 instruction accesses and 1 load.
.data
src: .word 42

.code
ld r1,src(r0)
syscall 0
