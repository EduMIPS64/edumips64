.data
var1: .double 0.5
var2: .double 0.1
.text
LDC1 f0,var1(r0)
SDC1 f0,var2(r0)
syscall 0