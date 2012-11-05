; Il programma legge un vettore di 10 double positivi ed un numero double 
; positivo, scorre il vettore e se l'elemento attuale è  minore 
; di 20 o divisibile per 2 allora lo eleva al quadrato, altrimenti gli sottrae il numero dato.


;by Simona Ullo, Massimo Trubia (FPU modification)

	.data
vet:	.double 	3.0,4.0,25.0,2.0,40.0,8.0,9.0,12.0,24.0
num:	.double 	1.0
venti:	.double	20.0
due: .double 2.0
zero: .double 0.0


	.code
	DADDI R1,R0,vet 	;R1=vet - indirizzo della variabile vet
	DADDI R2,R0,num		;R2=num - indirizzo della variabile num
	DSUB R3,R2,R1		;R3=num-vet - dimensione di vet
	DADDI R4,R0,0		;R4=0 - indice di vet
	LDC1 f5,venti(R0)		;R5=Mem[venti] - 20 (valore del confronto)
	LDC1 f6,num(R0)		;R6=Mem[num] - valore da sottrarre
    LDC1 F20,due(R0)
    LDC1 F21,zero(R0)

loop:	LDC1 f7,vet(R4)		;f7=Mem[vet+R4] - carica un elemento del vettore	
	c.lt.d 0,f7,f5		;if(f7<20) FCSR[0]=1
    nop 
	BC1T 0,quad  		;... allora eleva al quadrato l'elemento del vettore
	DIV.D f8,F7,F20		;verifica se l'elemento del vettore è pari
    ;es. 5.0/2.0=2.5    tronca(2.5)=2   2.5-tronca(2.5)=0.5   0.5>0 ==>numero dispari    
    CVT.L.D F10,f8      ;rimuove la parte decimale (settare il rounding mode verso lo zero)
    CVT.D.L F9,F10      ;porta il valore troncato in un altro registro
    SUB.D f11,f8,f9     ;memorizza in f11 l'eventuale resto della divisione
    C.LT.D 0,f21,f11  ; if(resto>0) FCSR[0]=1
    NOP 
	BC1F 0,quad		;... in tal caso lo eleva al quadrato
	SUB.D f7,f7,f6		;f7=f7-f6 sottrae num all'elemento del vettore
	ADD.D f29,f7,f21	;mette il valore in f29 per scriverlo in memoria
	J scrivi;	    	;salta per scrivere l'elemento modificato in memoria

quad:	ADD.D f30,f21,f7	;I parametro funzione - valore da elevare al quadrato
	JAL quadrato		;chiamata della funzione quadrato

scrivi:	SDC1 F29,vet(R4)		;Mem[vet+R4]=R7 memorizza il valore modificato
	DADDI R4,R4,8		;R4++
	SLT R8,R4,R3		;if(R4<R3) R8=1 
	BNEZ R8,loop		;se ci sono ancora elementi nel vettore ripete il ciclo
	HALT
		

;FUNZIONE QUADRATO

quadrato: 
	DADDI R28,R0,0		;R28=0 - indice ciclo
	ADD.D f29,f21,f21		;R29=R30
ciclo:
    ;f28=convert_double(r28)
    DMTC1 R28,f28       ;f28=r28
    CVT.D.L f31,f28     ;f31=convert_double(f28)
    MOV.D f28,f31       ;f28=f31
    
	C.LT.D 0,f28,f30		;if(f28<f29) FCSR[0]=1
	BC1F 0,ret		    ;ritorna al chiamante
	ADD.D F29,F29,F30	;f29+=f30   somma f29 a sè stesso
	DADDI R28,R28,1		;R28++   incrementa indice ciclo
	J ciclo			    ;cicla nuovamente
ret:	JR R31			;return

