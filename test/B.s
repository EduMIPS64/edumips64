.code
a: B d		; goto d
n: B f		; goto f
b: B g		; goto g
d: B b		; goto b
e: B i		; goto i
f: B h		; goto h
g: B Halt	; goto Halt
h: B e		; goto e
i: B c		; goto c
halt: syscall 0

; decomment the follow lines for testing parsing's errors
;   B unknow	; fake label
;1: B c		; number label  ; work :(
