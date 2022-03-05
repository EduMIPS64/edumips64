; Thanks to @hugmanrique for this code.

.data
v: .space 256
w: .space 256
x: .double 2.0
y: .double 1.5
z: .double 0.0

.text
daddi R1, R0, v
daddi R2, R0, w
daddi R3, R0, 256

ldc1 F2, x(R0)
ldc1 F4, y(R0)
daddi R4, R0, 0

loop1:
dmtc1 R4, F6
daddi R4, R4, 8
cvt.d.l F6, F6
mul.d F8, F2, F6
mul.d F10, F4, F6
dmtc1 R4, F12
daddi R4, R4, 8
cvt.d.l F12, F12
mul.d F14, F2, F12
daddi R1, R1, 16
mul.d F8, F8, F8
daddi R2, R2, 16

sdc1 F10, -16(R2)
mul.d F14, F14, F14
mul.d F16, F4, F12
sdc1 F8, -16(R1) ; here 
sdc1 F14, -8(R1)
sdc1 F16, -8(R2)
bne R4, R3, loop1
syscall 0