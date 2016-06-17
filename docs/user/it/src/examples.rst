Listati di esempio
==================
In questo capitolo sono presenti degli esemi di codice utili per comprendere
il funzionamento del simulatore.

SYSCALL
-------
Gli esempi per le SYSCALL 1-4 si riferiscono al file `print.s`, che è
l'esempio per la SYSCALL 5. Se si desidera eseguire gli esempi, è prima
necessario copiare il contenuto di quell'esempio in un file denominato
`print.s`, e salvarlo nella stessa directory contenente l'esempio che si sta
eseguendo.

Alcuni esempi si aspettano che esista un file descriptor, e non contengono il
codice per aprire alcun file. Per eseguire questi esempi, eseguire prima la
SYSCALL 1.

SYSCALL 0
~~~~~~~~~
L'effetto dell'esecuzione della SYSCALL 0 è l'interruzione dell'esecuzione del programma.
Esempio::
  .code
  daddi   r1, r0, 0    ; salva il valore 0 in R1
  syscall 0            ; termina l'esecuzione

SYSCALL 1
~~~~~~~~~
Programma d'esempio che apre un file::

                  .data 
  error_op:       .asciiz     "Errore durante l'apertura del file"    
  ok_message:     .asciiz     "Tutto ok."
  params_sys1:    .asciiz     "filename.txt"
                  .word64     0xF                    

                  .text
  open:           daddi       r14, r0, params_sys1    
                  syscall     1    
                  daddi       $s0, r0, -1
                  dadd        $s2, r0, r1        
                  daddi       $a0,r0,ok_message            
                  bne         r1,$s0,end            
                  daddi       $a0,r0,error_op

  end:            jal         print_string
                  syscall 0
          
                  #include    print.s      

Nelle prime due righe, vengono salvate in memoria le stringhe che contengono
i messaggi di errore e di successo, che saranno poi passati come parametri
alla funzione `print_string`, ed a ciascuno di essi viene associata
un'etichetta. La funzione `print_string` è presente nel file `print.s`.

Successivamente, vengono salvati in memoria i dati richiesti dalla SYSCALL 1,
il percorso del file da aprire (che deve esistere se si apre il file in
modalità sola lettura o lettura/scrittura) e, nella cella successiva, un
intero che definisce la modalità di apertura.

.. For more info about the opening mode of a file, please refer to \ref{sys1}.

In questo esempio, il file è stato aerto utilizzando la seguente modalità:
`O_RDWR` | `O_CREAT` | `O_APPEND`. Il numero 15 (0xF in base 16) deriva dalla
somma dei valori di queste tre modalità modes (3 + 4 + 8).

Questi due parametri hanno un'etichetta, in modo che in seguito possano essere
utilizzati.

Nella sezione .text, come prima cosa l'indirizzo di `param_sys1` - che per il
compilatore è un numero - viene salvato in r14; successivamente viene chiamata
la SYSCALL 1, ed il contenuto di R1 viene salvato nel registro $s2, in modo
che possa essere utilizzato nel resto del programma (ad esempio, con un'altra
SYSCALL).

Infine viene chiamata la funzione `print_string`, passando come parametro
`error_op` se R1 contiene il valore -1 (righe 13-14), altrimenti utlizzando
`ok_message` (righe 12-16).

SYSCALL 2
~~~~~~~~~
Programma di esempio che chiude un file::

                  .data
  params_sys2:    .space 8
  error_cl:       .asciiz     "Errore durante la chiususra del file"
  ok_message:     .asciiz     "Tutto a posto"

                  .text
  close:          daddi       r14, r0, params_sys2        
                  sw          $s2, params_sys2(r0)    
                  syscall     2            
                  daddi       $s0, r0, -1        
                  daddi       $a0, r0, ok_message            
                  bne         r1, $s0, end            
                  daddi       $a0, r0, error_cl

  end:            jal         print_string
                  syscall     0
      
                  #include    print.s         

**Nota:** Questo esempio richiede che in $s2 ci sia il file descriptor del
file da chiudere.

Come prima cosa viene allocata della memoria per l'unico parametro di SYSCALL
2, il file descriptor del file da chiudere, e a questo spazio viene associata
un'etichetta in modo da potervicisi riferire successivamente.

Successivamente vengono salvate in memoria le stringhe contenenti i messaggi
di successo e di errore.

Nella sezione .text, l'indirizzo di `param_sys2` viene salvato in R14;
successivamente viene chiamata la SYSCALL 2.

Infine viene chiamata la funzione `print_string`, stampando il messaggio
d'errore se ci sono problemi (riga 13) o, se tutto è andato a buon fine, il
messaggio di successo (riga 11).

SYSCALL 3
~~~~~~~~~
Programma di esempio che legge 16 byte da un file e li salva in memoria::

                  .data
  params_sys3:    .space      8                
  ind_value:      .space      8            
                  .word64     16        
  error_3:        .asciiz     "Errore durante la lettura da file."    
  ok_message:     .asciiz     "Tutto ok."    

  value:          .space      30                    

                  .text
  read:           daddi       r14, r0, params_sys3 
                  sw          $s2, params_sys3(r0)
                  daddi       $s1, r0, value            
                  sw          $s1, ind_value(r0)            
                  syscall     3            
                  daddi       $s0, r0, -1            
                  daddi       $a0, r0,ok_message            
                  bne         r1, $s0,end            
                  daddi       $a0, r0,error_3

  end:            jal         print_string
                  syscall     0
          
                  #include    print.s 

Le prime 4 righe della sezione .data contengono i parametri della SYSCALL 3,
il file descriptor da cui si devono leggere i dati, l'indirizzo della cella di
memoria dove la SYSCALL deve salvare i dati letti, il numero di byte da
leggere. Successivamente sono presenti in memoria i messaggi di successo e di
errore.

Nella sezione .text, come prima cosa viene salvato l'indirizzo di `param_sys3`
in r14, il file descriptor viene salvato nell'area di memoria dedicata ai
parametri della SYSCALL, ed a seguire lo stesso destino tocca all'indirizzo
dell'area di memoria adibita a contenere i dati letti.

Successivamente viene chiamata la SYSCALL 3 e viene stampato un messaggio di
successo o di errore, a seconda dell'esito della SYSCALL.

SYSCALL 4
~~~~~~~~~
Programma di esempio che scrive su file una stringa::

                  .data
  params_sys4:    .space      8                
  ind_value:      .space      8            
                  .word64     16        
  error_4:        .asciiz     "Errore durante la scrittura su stringa."    
  ok_message:     .asciiz     "Tutto ok."    
  value:          .space      30                    

                  .text
              
  write:          daddi       r14, r0,params_sys4        
                  sw          $s2, params_sys4(r0)        
                  daddi       $s1, r0,value            
                  sw          $s1, ind_value(r0)            
                  syscall     4                
                  daddi       $s0, r0,-1
                  daddi       $a0, r0,ok_message            
                  bne         r1, $s0,end            
                  daddi       $a0, r0,error_4

  end:            jal         print_string
                  syscall     0
          
                  #include    print.s 

La struttura di quest'esempio è identica a quella dell'esempio di SYSCALL 3.

SYSCALL 5
~~~~~~~~~
Programma di esempio che contiene una funzione che stampa su standard output la
stringa contenuta nell'indirizzo di memoria a cui punta $a0::

                  .data
  params_sys5:    .space  8

                  .text
  print_string:   
                  sw      $a0, params_sys5(r0)    
                  daddi   r14, r0, params_sys5
                  syscall 5
                  jr      r31

La seconda riga alloca spazio per la stringa che sarà stampata dalla SYSCALL,
che è riempito dalla prima istruzione della sezione .text, che assume che
l'indirizzo della stringa da stampare sia in $a0.

L'istruzione successiva salva in r14 l'indirizzo di questa stringa, e
successivamente la SYSCALL 5 viene chiamata, stampando quindi la stringa.
L'ultima istruzione varia il program counter, impostandolo al valore di r31 -
che secondo le convenzioni di chiamata di funzione MIPS contiene l'indirizzo
dell'istruzione successiva alla chiamata di funzione.


Un esempio di utilizzo della SYSCALL 5 più complesso
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
La SYSCALL 5 utilizza un meccanismo di passaggio parametri non semplicissimo,
che sarà illustrato nel seguente esempio::

                  .data
  format_str:     .asciiz   "%d %s:\nTest di %s versione %i.%i!"
  s1:             .asciiz   "Giugno"
  s2:             .asciiz   "EduMIPS64"
  fs_addr:        .space    4
                  .word     5    
  s1_addr:        .space    4
  s2_addr:        .space    4
                  .word     0
                  .word     5
  test:
                  .code
                  daddi     r5, r0, format_str
                  sw        r5, fs_addr(r0)
                  daddi     r2, r0, s1
                  daddi     r3, r0, s2
                  sd        r2, s1_addr(r0)
                  sd        r3, s2_addr(r0)
                  daddi     r14, r0, fs_addr
                  syscall   5
                  syscall   0

L'indirizzo di memoria della stringa di formato viene inserito in R5, il cui
contenuto viene quindi salvato in memoria all'indirizzo `fs_addr`. Gli
indirizzi dei parametri di tipo stringa sono salvato in `s1_addr` ed
`s2_addr`. Questi due parametri saranno inseriti al posto dei due segnaposto
`%s` all'interno della stringa di formato.

Nel caso di stringhe di formato complesse, come mostrato da questo esempio, le
word che corrispondono ai segnaposto vanno inserite in memoria subito dopo
l'indirizzo della stringa di formato.
