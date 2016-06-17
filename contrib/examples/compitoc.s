;Massimo Trubia

; Dati un vettore vett1={1.0, 2.0, 6.0, 16.0, 8.0, 3.0, 14.0, 12.0, 10.0} si realizzi un
; programmache:
; (a) Visualizza gli elementi di vett1 di valore pari che NON sono multipli di 4
; (b) Crea un vettore vett2, con vett2[i] = vett1[i] * max(vett1)
; (c) Visualizza gli elementi di vett2 in posizione dispari (il primo, il terzo etc..)
		.data
vett1:	.double 1.0, 2.0, 6.0, 16.0, 8.0, 3.0, 14.0, 12.0, 10.0
vett1_pari: .double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett2:	.double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
vett2_dispari: .double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
due: .double 2.0
quattro: .double 4.0
		.text
        ldc1 f30,due(r0)
        ldc1 f31,quattro(r0)
        mtc1 r0,f29; zero
		daddi r1,r0,vett1
		daddi r2,r0,vett1_pari
		dsub r3,r2,r1		;r3=size=vett2[]-vett1[]
		daddi r1,r0,0		;r1=i=0
		daddi r2,r0,0		;r2=i2=0
		daddi r4,r0,0		;r4=max=0
whilescanvett1: slt r5,r1,r3 		;while(i<size)
		beqz r5,endwhilescanvett1
		ldc1 f6,vett1(r1)		;f6=vett1[i]
ifprimo:	bnez r1,ifmax		;if(i==0) then 
		mov.d f4,f6		;f4=max=vett1[i];
ifmax:	c.lt.d 0,f4,f6
        nop
		bc1f 0,ifpari
		mov.d f4,f6		    ;f4=max=vett1[i];
ifpari:	                       
        ;es. check if 11.0 is an even value (11.0/2.0)=5.5;  trunc(5.5)=5;  5.5-trunc(5.5)=0.5   0.5>0 not even
        div.d f8,f6,f30
        cvt.l.d f20,f8      ; f20=trunc(f8)
        cvt.d.l f21,f20      ;f21= trunc(f8)
        sub.d f20,f8,f21    ;f20=resto(f6/2.0)
        c.eq.d 0,f29,f20  ; FCSR[0]=(f29==f20)
        nop
        bc1f 0,endifpari  
        
ifnomult4: div.d f8,f6,f31 ;if(vett1[i]%4!=0)
        cvt.l.d f20,f8
        cvt.d.l f21,f20
        sub.d f20,f8,f21
        c.eq.d 0, f29,f20 ; FCSR[0]=(f29==f20)
        nop
        bc1t 0,endifpari
		sdc1 f6,vett1_pari(r2)
		daddi r2,r2,8
endifpari:	daddi r1,r1,8
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
ifdispari:	bnez r10,endifdispari 	;if(i%2==0)		
		ldc1 f11,vett2(r1)		;f11=vett2[i];
		sdc1 f11,vett2_dispari(r2)
		daddi r2,r2,8
endifdispari: daddi r1,r1,8
		j whilescanvett2
endwhilescanvett2:	syscall 0	
		
		
		
