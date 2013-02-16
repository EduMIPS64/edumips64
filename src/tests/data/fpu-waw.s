.data
var1: .double 3.5
var2: .double 4.5
.text
ldc1 f2,var1(r0)
ldc1 f3,var2(r0)
mul.d f4,f3,f2
add.d f4,f3,f2
halt
