;SPECIAL VALUES FOR div.d
.data
var1: .double POSITIVEINFINITY       ;0X7FF0000000000000; PLUS INFINITY
var2: .double NEGATIVEINFINITY       ;0xFFF0000000000000; MINUS INFINITY
var3: .double POSITIVEZERO           ;0x0000000000000000; PLUS ZERO
var4: .double NEGATIVEZERO           ;0x8000000000000000; MINUS ZERO
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

;//operations between infinities (2 examples)
div.d f10,f0,f0 ; +Infinity  /  +Infinity = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
div.d f11,f0,f1 ; +Infinity  /  -Infinity = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)

;//operations between infinities and signed numbers
div.d f22,f0,f6     ; +Infinity / positive number = +Infinity
div.d f23,f0,f28    ; +Infinity / negative number = -Infinity
div.d f24,f1,f6     ; -Infinity / positive number = -Infinity
div.d f29,f1,f28    ; -Infinity / negative number = +Infinity



;//operations betwen zeros (1 example)
div.d f30,f2,f3     ; +Zero / -Zero = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)

; any/zero (1 example)
div.d f31,f6,f2        ;a positive number / +0 =+Infinity(if DivisionByZeroException disabled)                      // trap(if DivisionByZeroException enabled) 


;zero/any   
div.d f12,f3,f6         ; -Zero / a positive number = -Zero

;infinity/any
div.d f13,f1,f6         ; -Infinity / a positive number= -Infinity

;//operations containing qnans
div.d f14,f5,f6 ;  QNaN  / number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
div.d f15,f6,f5 ;  number / QNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//operations containing sNaNs
div.d f16,f4,f6 ;  SNaN / number = QNaN(if InvalidOperationException disabled)           //Trap (if InvalidOperationException enabled)
div.d f17,f6,f4 ;  number / SNaN = QNaN(if InvalidOperationException disabled)            //Trap (if InvalidOperationException enabled)

;//raising overflows
div.d f18,f6,f27 ; positive number / positive  very small number = +Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)
div.d f19,f28,f27 ; negative number / positive very small number = -Infinity(if FPOverflowException disabled)      //Trap(if FPOverflowException enabled)

;//raising underflows  
div.d f20,f9,f7 ; a positive small number /  a positive big number= +Zero(if FPUnderflowException disabled)       //Trap(if FPUnderflowException enabled)
div.d f21,f25,f7 ; a negative small number / a positive big number= -Zero(if FPUnderflowException disabled)      //Trap(if FPUnderflowException enabled)
syscall 0