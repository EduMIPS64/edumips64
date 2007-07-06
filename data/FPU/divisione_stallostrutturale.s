; in qualunque situazione se una div.d ha finito il calcolo deve uscire
; dalla pipeline quindi in uscita non si può avere stallo strutturale in cui
; la div.d deve fermarsi
.text
div.d f3,f1,f2
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
add.d f5,f6,f7
nop
nop
nop
dadd r5,r6,r7
halt

