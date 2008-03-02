#include pippo.s
        .data
vec1:   .byte   1,2,3,4,5,6,7,8
vec2:   .byte   1,2,3,4,5,6,7,8
vec3:   .byte   1,2,3,4,5,6,7,8

        .code
start:  daddi   r1, r0, 0
second: daddi   r2, r0, 0
third:  daddi   r3, r0, 0
        B       end
        nop
        nop
        nop
end:    daddi   r4, r0, 0
        syscall 0

