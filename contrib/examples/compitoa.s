;Massimo Trubia
; Given a vector vett1={1.8,2.4,7.6,4.6,5.8,9.9,11.2,3.0,32.0,16.0}, write a program that:
; a) copies elements of vett1 less than 5.5 into a vector2, reversing their order
; b) creates the double vector vett3, where vett3[i]=vett2[i]/vett1[i]
; c) displays the minimum of vett3 and its position
		.data
vett1:	.double 1.8,2.4,7.6,4.6,5.8,9.9,11.2,3.0,32.0,16.0
vett2:	.double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett2_temp: .double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett3:	.double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
limit: .double 5.5
		.text
		DADDI R1,R0,vett1		;r1=vett1[]
		daddi r2,r0,vett2		;r2=vett2[]
		dsub r3,r2,r1			;r3=size
		daddi r1,r0,0			;r1=i=0
		daddi r2,r0,0			;r2=i2=0
        ldc1 f0,limit(r0)
whilescannumbers:	slt r4,r1,r3			        ;while(i<size)
		beqz r4,endwhilescannumbers
		LDC1 f5,vett1(r1)			;f5=vett1[i]
        c.lt.d 0,f0,f5
        nop
iflessthanlimit:	BC1T 0,endiflessthanlimit              
        SDC1 f5,vett2_temp(r2)
		daddi r2,r2,8
endiflessthanlimit:	daddi r1,r1,8
		j whilescannumbers	
endwhilescannumbers:  daddi r1,r0,0	;r1=i=0
		dmtc1 r0,f7 			;f7=min=0
		daddi r8,r0,0			;r8=minindex=0
		dadd r9,r0,r2			;r9=vett2_size=i2
whilescanvet2: slt r10,r1,r9			;while(i<vett2_size) 
		beqz r10,endwhilescanvet2
		;perform vett2[i]=vett2_temp[i2-1]
		daddi r11,r0,1
		dsub r11,r2,r11			;r11=[i2-1]
		ldc1 f12,vett2_temp(r11)	;f12=vett2_temp[i2-1]
		sdc1 f12,vett2(r1)			;r12=vett2[i]=vett2_temp[i2-1]
		;perform vett3[i]=vett2[i]/vett1[i];
		ldc1 f13,vett1(r1)			;r13=vett1[i]
		div.d f12,f12,f13       ;f12=vett2[i]/vett1[i];
		sdc1 f12,vett3(r1)		;vett3[i]=vett2[i]/vett1[i];
iffirst:	bnez r1,ifminimum
		ldc1 f7,vett3(r1)			;r7=min=vett3[i]
		dadd r8,r0,r1			;r8=min_index=i;
ifminimum: 	c.lt.d 0,f12,f7			;if(vett2[i]<min)
        nop
		bc1t 0,incr
		ldc1 f7,vett3(r1)			;min=vett3[i];
		dadd r8,r0,r1			;min_index=i;
incr:		daddi r1,r1,8			;i++
		daddi r15,r0,8
		dsub r2,r2,r15			;i2--
		j whilescanvet2
endwhilescanvet2: syscall 0			;results: f7=min(vett3)   r8=min.index()
		

