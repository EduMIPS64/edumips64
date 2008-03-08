.data
var1: .double POSITIVEINFINITY       ;0X7FF0000000000000; PLUS INFINITY
var2: .double NEGATIVEINFINITY       ;0xFFF0000000000000; MINUS INFINITY
var3: .double POSITIVEZERO           ;0x0000000000000000; PLUS ZERO
var4: .double NEGATIVEZERO           ;0x8000000000000000; MINUS INFINITY
var5: .double SNAN                   ;0x7FFFFFFFFFFFFFFF; SNAN
var6: .double QNAN                   ;0x7ff7ffffffffffff; QNAN

var7: .word 3534535 ; a number
var8: .double 1.7E308 ; a positive big number
var9: .double -1.7E308; a negative big number
var10: .double 9.0E-324; a positive small number
var11: .double -6.0E-324; a negative small number
var12: .double -7.0E-324; a negative small number
var13: .double 5.0E-324; a positive small number

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

;//operations between infinities
add.d f10,f0,f0 ; +Infinity  +  +Infinity = +Infinity
add.d f11,f0,f1 ; +Infinity  +  -Infinity = QNAN(if InvalidOperationException disabled)   Trap (if InvalidOperationException enabled)
add.d f12,f1,f1 ; -Infinity  +  -Infinity = -Infinity   
add.d f13,f1,f0 ; -Infinity  +  +Infinity = QNan(if InvalidOperationException disabled)   //Trap (if InvalidOperationException enabled)

;//operations containing qnans
add.d f14,f5,f6 ;  QNaN  + number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
add.d f15,f6,f5 ;  number + QNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//operations containing sNaNs
add.d f16,f4,f6 ;  SNaN + number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
add.d f17,f6,f4 ;  number + SNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//raising overflows
add.d f18,f7,f7 ; positive big number + positive big number = +Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)
add.d f19,f8,f8 ; negative big number + negative big number = -Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)

;//raising underflows  
add.d f20,f9,f25 ; a positive small number +  a negative small number= +Zero(if FPUnderflowException disabled)       //Trap(if FPUnderflowException enabled)
add.d f21,f26,f27 ; a negative small number + a positive small number= -Zero(if FPUnderflowException disabled)(non riesco a generare l'underflow,rivedere)      //Trap(if FPUnderflowException enabled)

halt
