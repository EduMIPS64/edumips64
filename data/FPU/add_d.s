.data
var1: .double 1.5
var2: .double 3.5
.text
LDC1 f0,var1(r0)
LDC1 f1,var2(r0)
add.d f2,f0,f1
syscall 0