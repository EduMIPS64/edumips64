.data
var1: .double 10.0
var2: .double 2.0
var3: .double 20.5
var4: .double 19.5
.text
ldc1 f1,var1(r0)
ldc1 f2,var2(r0)
ldc1 f5,var3(r0)
ldc1 f6,var4(r0)
;l.d f1,var1(r0)
;l.d f2,var2(r0)
;l.d f5,var3(r0)
;l.d f6,var4(r0)
div.d f0,f1,f2
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
add.d f4,f5,f6
halt