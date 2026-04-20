;Massimo Trubia

; Given a vector vett1={1.0, 2.0, 6.0, 16.0, 8.0, 3.0, 14.0, 12.0, 10.0}, write a program that:
; (a) Displays the even-valued elements of vett1 that are NOT multiples of 4
; (b) Creates a vector vett2, with vett2[i] = vett1[i] * max(vett1)
; (c) Displays the elements of vett2 at odd positions (the first, the third, etc.)
		.data
vett1:	.double 1.0, 2.0, 6.0, 16.0, 8.0, 3.0, 14.0, 12.0, 10.0
vett1_even: .double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett2:	.double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett2_odd: .double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
two: .double 2.0
four: .double 4.0
		.text
        ldc1 f30,two(r0)
        ldc1 f31,four(r0)
        mtc1 r0,f29; zero
		daddi r1,r0,vett1
		daddi r2,r0,vett1_even
		dsub r3,r2,r1		;r3=size=vett2[]-vett1[]
		daddi r1,r0,0		;r1=i=0
		daddi r2,r0,0		;r2=i2=0
		daddi r4,r0,0		;r4=max=0
whilescanvett1: slt r5,r1,r3 		;while(i<size)
		beqz r5,endwhilescanvett1
		ldc1 f6,vett1(r1)		;f6=vett1[i]
iffirst:	bnez r1,ifmax		;if(i==0) then 
		mov.d f4,f6		;f4=max=vett1[i];
ifmax:	c.lt.d 0,f4,f6
        nop
		bc1f 0,ifeven
		mov.d f4,f6		    ;f4=max=vett1[i];
ifeven:	                       
        ;es. check if 11.0 is an even value (11.0/2.0)=5.5;  trunc(5.5)=5;  5.5-trunc(5.5)=0.5   0.5>0 not even
        div.d f8,f6,f30
        cvt.l.d f20,f8      ; f20=trunc(f8)
        cvt.d.l f21,f20      ;f21= trunc(f8)
        sub.d f20,f8,f21    ;f20=remainder(f6/2.0)
        c.eq.d 0,f29,f20  ; FCSR[0]=(f29==f20)
        nop
        bc1f 0,endifeven  
        
ifnomult4: div.d f8,f6,f31 ;if(vett1[i]%4!=0)
        cvt.l.d f20,f8
        cvt.d.l f21,f20
        sub.d f20,f8,f21
        c.eq.d 0, f29,f20 ; FCSR[0]=(f29==f20)
        nop
        bc1t 0,endifeven
		sdc1 f6,vett1_even(r2)
		daddi r2,r2,8
endifeven:	daddi r1,r1,8
		j whilescanvett1
endwhilescanvett1: daddi r1,r0,0
		daddi r2,r0,0
whilescanvett2: slt r9,r1,r3
		beqz r9,endwhilescanvett2
		ldc1 f6,vett1(r1)
		;vett2[i]=vett1[i]*max;
        mul.d f7,f6,f4
		sdc1 f7,vett2(r1)
		daddi r14,r0,8
		ddiv r1,r14		;LO=i/8
		mflo r15
		andi r10,r15,1 ;andi r10,LO,1
ifodd:	bnez r10,endifodd 	;if(r15%2==0), i.e., the element is at an even index (0, 2, 4...) meaning 1st, 3rd, 5th position		
		ldc1 f11,vett2(r1)		;f11=vett2[i];
		sdc1 f11,vett2_odd(r2)
		daddi r2,r2,8
endifodd: daddi r1,r1,8
		j whilescanvett2
endwhilescanvett2:	syscall 0	
		
		
		
