;Massimo Trubia
;Dato un vettore di numeri interi {3.6,40.7,5.67,35.4,6.435,7.6547,46.546,35.454,9.4560,10.456}, 
;copiare tutti i numeri che si trovano all'esterno dell'intervallo [6.0;30.0] 
;in un secondo vettore, di cui poi bisogna calcolare la somma.
;le cache separate hanno diversa associatività
		.data
vett1:	.double 3.6,40.7,5.67,35.4,6.435,7.6547,46.546,35.454,9.4560,10.456
vett2:	.double 0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
rangelow: .double 6.0
rangehi: .double 30.0
		.text
		daddi r1,r0,vett1
		daddi r2,r0,vett2
		dsub r3,r2,r1			;r3=sizeof(vett1)
		daddi r1,r0,0			;r1=i=0
		daddi r2,r0,0			;r2=i2=0
		daddi r4,r0,0			;r4=somma=0
while_scanvett1:
		slt r5,r1,r3				;while(i<size)
		beqz r5,endwhile_scanvett1
		ldc1 f6,vett1(r1)			;è é f6=(vett1[i])
		ldc1 f7,rangelow(r0)
		c.lt.d 0,f6,f7				;é FCSR[0]=(vett1[i]<6)
        nop
		bc1t 0,outofrange
		ldc1 f7,rangehi(r0)
		c.lt.d 0,f7,f6
        nop
		bc1t 0,outofrange
		j incr
outofrange:	 
		sdc1 f6,vett2(r2)			;vett2[i2]=vett1[i]
		add.d f4,f4,f6			;somma+=vett2[i2]
		daddi r2,r2,8			;i2++
endoutofrange:
incr:		daddi r1,r1,8
		j while_scanvett1
endwhile_scanvett1: syscall 0			;somma in f4 
		

; public class Compito
; {
	; public static void main(String[] args)
	; {
		; int vett1[]={3,40,5,35,6,7,46,35,9,10};
		; int vett2[]={0,0,0,0,0,0,0,0,0,0};
		; int size=vett1.length;
		; int i=0;
		; int i2=0;
		; int somma=0;
		; while(i<size)
		; {
			; if((vett1[i]<6) || (vett1[i]>30))
			; {
				
				; vett2[i2]=vett1[i];
				; somma+=vett2[i2];
				; i2++;
			; }
			; i++;
		; }
		; System.out.println(somma); //164  3,40,5,35,46,35
	; }
; }