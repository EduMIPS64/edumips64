.data
var10: .double -9.456456E-324; a positive small number
var11: .double 5.856456456E-324; a negative small number
.text
;//raising overflows
ldc1 f0,var10(r0)
ldc1 f1,var11(r0)
add.d f2,f0,f1