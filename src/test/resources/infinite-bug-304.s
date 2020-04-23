;--------------------------------------------------------------------------
;
;
; THIS CODE demonstrates an apparent bug in EDUMIPS.
; Tested using EduMIPS64 v1.2.6 (standalone) and v1.2.5
;
;--------------------------------------------------------------------------
               .data         ; start of data space

scalara:       .double  -3.7 ; first scalar
scalarb:       .double  5.1  ; second scalar
vectorX:       .double  -5.78;
vectorXnext:   .double  10.26;
vectorXnext2:  .double  -1.9,    0.001,   3.002, -1257.72,  1009.235,    899.0001,  9.9999, 0.000, -30.567, -10020.5  ; first vector
vectorY:       .double  -4.88;
vectorYnext:   .double  202.21;
vectorYnext2:  .double  31.8, -206.112, 100.7,     -88.12,      0.77,  10031.4,    -3.02,   0.000,  15.5,      622.80 ; second vector
vectorZ:       .space  96    ; space set aside for result

;--------------------------------------------------------------------------
; CODE SPACE
;--------------------------------------------------------------------------
               .text        ; start of code space

main:
          L.D f1, scalara(r0) 
          L.D f2, scalarb(r0)
          DADDI r3, r0, 0

          DADDI r1, r0, #88 ; 

          L.D f3, vectorX(r3) 
          L.D f4, vectorY(r3) 
          MUL.D f5, f1, f3     
          MUL.D f6, f2, f4   

          L.D f3, vectorXnext(r3) 
          L.D f4, vectorYnext(r3) 

          ADD.D f7, f5, f6
 
          MUL.D f5, f1, f3   ; THIS INSTRUCTION'S WB HAPPENS, but the CYCLE WINDOW does not show the MEM and WB stages.
          MUL.D f6, f2, f4   ; THIS INSTRUCTION'S WB HAPPENS, but the CYCLE WINDOW does not show the M7, MEM and WB stages.

loop:      S.D f7, vectorZ(r3) 

           L.D f3, vectorXnext2(r3) 
           L.D f4, vectorYnext2(r3) 
           ;DADDI r5, r0, 0   ; DUMMY INSTRUCTION INSERTED TO CIRCUMVENT BUG.
           ADDI r3, r3, 8     ; THIS INSTRUCTION'S WB NEVER HAPPENS.  CYCLE WINDOW shows no MEM OR WB stages.
           ADD.D f7, f5, f6   
           MUL.D f5, f1, f3   
           MUL.D f6, f2, f4   
           BNE r3, r1, loop   ; CONSEQUENTLY, THIS INSTRUCTION INFINITELY RAW STALLS.

           SYSCALL 0


