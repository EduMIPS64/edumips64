;2 istruzioni possono occupare la stessa unità funzionale? SI
.data
var1: .double 3.5
var2: .double 4.5
prod: .double 0
.text
l.d f2,var1(r0)
l.d f3,var2(r0)
div.d f1,f3,f2
div.d f4,f3,f2
div.d f5,f3,f2
div.d f6,f3,f2
div.d f7,f3,f2
div.d f8,f3,f2
div.d f9,f3,f2
div.d f10,f3,f2
div.d f11,f3,f2
div.d f12,f3,f2
div.d f13,f3,f2
div.d f14,f3,f2
div.d f15,f3,f2
div.d f16,f3,f2
div.d f17,f3,f2
div.d f18,f3,f2
div.d f19,f3,f2
div.d f20,f3,f2
div.d f21,f3,f2
div.d f22,f3,f2
div.d f23,f3,f2
div.d f24,f3,f2
div.d f25,f3,f2
div.d f26,f3,f2
halt