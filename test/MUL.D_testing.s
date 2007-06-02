;SPECIAL VALUES FOR MUL.D
.data
var1: .double POSITIVEINFINITY       ;0X7FF0000000000000; PLUS INFINITY
var2: .double NEGATIVEINFINITY       ;0xFFF0000000000000; MINUS INFINITY
var3: .double POSITIVEZERO           ;0x0000000000000000; PLUS ZERO
var4: .double NEGATIVEZERO           ;0x8000000000000000; MINUS INFINITY
var5: .double SNAN                   ;0x7FFFFFFFFFFFFFFF; SNAN
var6: .double QNAN                   ;0x7ff7ffffffffffff; QNAN

var7: .double 1.5 ; a positive number
var14: .double -1.5; a negative number
var8: .double 1.7E308 ; a positive big number
var9: .double -1.7E308; a negative big number
var10: .double 9.0E-324; a positive small number
var11: .double -9.0E-324; a negative small number
var12: .double -6.0E-324; a negative very small number
var13: .double 6.0E-324; a positive very small number

.text
ldc1 f0,var1(r0)
ldc1 f1,var2(r0)
ldc1 f2,var3(r0) 
ldc1 f3,var4(r0)
ldc1 f4,var5(r0)
ldc1 f5,var6(r0)
ldc1 f6,var7(r0)
ldc1 f7,var8(r0)
ldc1 f8,var9(r0)
ldc1 f9,var10(r0)
ldc1 f25,var11(r0)
ldc1 f26,var12(r0)
ldc1 f27,var13(r0)
ldc1 f28,var14(r0)

;//operations between infinities
mul.d f10,f0,f0 ; +Infinity  X  +Infinity = +Infinity
mul.d f11,f0,f1 ; +Infinity  X  -Infinity = -Infinity
mul.d f12,f1,f1 ; -Infinity  X  -Infinity = +Infinity
mul.d f13,f1,f0 ; -Infinity  X  +Infinity = -Infinity

;//operations between infinities and signed numbers
mul.d f22,f0,f6     ; +Infinity X positive number = +Infinity
mul.d f23,f0,f28    ; +Infinity X negative number = -Infinity
mul.d f24,f1,f6     ; -Infinity X positive number = -Infinity
mul.d f29,f1,f28    ; -Infinity X negative number = +Infinity

;//operations betwen zeros (1 example)
mul.d f30,f2,f1     ; +Zero X -Zero = -Zero

;//operations containing qnans
mul.d f14,f5,f6 ;  QNaN  X number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
mul.d f15,f6,f5 ;  number X QNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//operations containing sNaNs
mul.d f16,f4,f6 ;  SNaN X number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
mul.d f17,f6,f4 ;  number X SNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//raising overflows
mul.d f18,f7,f7 ; positive big number X positive big number = +Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)
mul.d f19,f7,f8 ; positive big number X negative big number = -Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)

;//raising underflows  
mul.d f20,f9,f27 ; a positive small number X  a positive very small number= +Zero(if FPUnderflowException disabled)       //Trap(if FPUnderflowException enabled)
mul.d f21,f25,f27 ; a negative small number X a positive very small number= -Zero(if FPUnderflowException disabled)      //Trap(if FPUnderflowException enabled)
syscall 0