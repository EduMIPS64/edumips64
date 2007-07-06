.data
var1: .double 3.5
var2: .double 4.5
prod: .double 0
.text
l.d f2,var1(r0)
l.d f3,var2(r0)
mul.d f4,f3,f2
add.d f4,f3,f2
add.d f5,f6,f7
halt