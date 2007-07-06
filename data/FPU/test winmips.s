.data
var1: .double 3.5
var2: .double 4.5
var3: .word 5
var4: .word 6
.text
l.d f1,var1(r0)
l.d f2,var2(r0)
ld r4,var3(r0)
ld r5,var4(r0)
add.d f3,f1,f2
dadd r6,r4,r5
halt

