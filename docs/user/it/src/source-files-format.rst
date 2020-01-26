Formato dei file sorgenti
=========================

EduMIPS64 si propone di seguire le convenzioni usate negli altri simulatori
MIPS64 e DLX, in modo tale da non creare confusione riguardante la sintassi
per i vecchi utenti.

All'interno di un file sorgente sono presenti due sezioni: quella dedicata ai
*dati* e quella in cui è contenuto il *codice*, introdotte rispettivamente
dalle direttive *.data* e *.code*. Nel seguente listato è possibile vedere un
semplice programma::

    ; Questo è un commento
              .data
      label:  .word   15     ; Questo è un commento in linea

              .code
              daddi   r1, r0, 0
              syscall 0

Per distinguere le varie parti di ciascuna linea di codice, può essere
utilizzata una qualunque combinazione di spazi e tabulazioni, visto che il
parser ignora spazi multipli.

I commenti possono essere introdotti utilizzando il carattere ";" qualsiasi
cosa venga scritta successivamente ad esso verrà ignorata.  Un commento
può quindi essere usato "inline" (dopo una direttiva) oppure in una riga a
sè stante.

Le etichette possono essere usate nel codice per fare riferimento ad una cella
di memoria o ad un'istruzione.  Esse sono case insensitive. Per ciascuna linea
di codice può essere utilizzata un'unica etichetta. Quest'ultima può essere
inserita una o più righe al di sopra dell'effettiva dichiarazione del dato
o dell'istruzione, facendo in modo che non ci sia nulla, eccetto commenti e
linee vuote, tra l'etichetta stessa e la dichiarazione.

Limiti di memoria
-----------------

EduMIPS64 ha memoria limitata, sia per i dati (sezione `.data`, limitata a 640
kB -- i.e., 80000 valori a 64 bit) che per le istruzioni (sezione `.code`,
limitata a 128 kB -- i.e., 32000 istruzioni, ciascuna da 32 bit).

I limiti sono arbitrari e fissati nel codice del simulatore.

La sezione `.data`
------------------
La sezione *data* contiene i comandi che specificano il modo in cui la
memoria deve essere riempita prima dell'inizio dell'esecuzione del programma.
La forma generale di un comando `.data` è::

  [etichetta:] .tipo-dato valore1 [, valore2 [, ...]]

EduMIPS64 supporta diversi tipi di dato, che sono descritti nella seguente
tabella.

            =========== ==================== =============
            Tipo        Direttiva            Bit richiesti
            =========== ==================== =============
            Byte        `.byte`              8
            Half word   `.word16`            16
            Word        `.word32`            32
            Double Word `.word` or `.word64` 64
            =========== ==================== =============

Dati di tipo doubleword possono essere introdotti sia dalla direttiva
`.word` che dalla direttiva `.word64`.

I tipi di dato sono interpretati con segno. Questo significa che tutte le
costanti intere nella sezione `.data` devono essere compresi tra -2^(n-1) e
2^(n-1) - 1 (estremi inclusi).

Esiste una differenza sostanziale tra la dichiarazione di una lista di dati
utilizzando un'unica direttiva oppure direttive multiple dello stesso tipo.
EduMIPS64 inizia la scrittura a partire dalla successiva double word a 64 bit
non appena trova un identificatore del tipo di dato, in tal modo la prima
istruzione `.byte` del seguente listato inserirà i numeri 1, 2, 3 e 4 nello
spazio di 4 byte, occupando 32 bit, mentre il codice delle successive quattro
righe inserirà ciascun numero in una differente cella di memoria, occupando
32 byte::

    .data
    .byte    1, 2, 3, 4
    .byte    1
    .byte    2
    .byte    3
    .byte    4

Nella seguente tabella, la memoria è rappresentata utilizzando celle di
dimensione pari ad 1 byte e ciascuna riga è lunga 64 bit. L'indirizzo posto
alla sinistra di ogni riga della tabella è riferito alla cella di memoria più
a destra, che possiede l'indirizzo più basso rispetto alle otto celle in
ciascuna linea.

+----+-+-+-+-+-+-+-+-+
|*0* |0|0|0|0|4|3|2|1|
+----+-+-+-+-+-+-+-+-+
|*8* |0|0|0|0|0|0|0|1|
+----+-+-+-+-+-+-+-+-+
|*16*|0|0|0|0|0|0|0|2|
+----+-+-+-+-+-+-+-+-+
|*24*|0|0|0|0|0|0|0|3|
+----+-+-+-+-+-+-+-+-+
|*36*|0|0|0|0|0|0|0|4|
+----+-+-+-+-+-+-+-+-+

Ci sono alcune direttive speciali che devono essere discusse: `.space`,
`.ascii` e `.asciiz`.

La direttiva `.space` è usata per lasciare dello spazio vuoto in memoria.
Essa accetta un intero come parametro, che indica il numero di byte che devono
essere lasciati liberi.  Tale direttiva è utile quando è necessario conservare
dello spazio in memoria per i risultati dei propri calcoli.

La direttiva `.ascii` accetta stringhe contenenti un qualunque carattere
ASCII, ed alcune "sequenze di escape", simili a quelle presenti nel linguaggio
C, che sono descritte nella seguente tabella, ed inserisce tali stringhe in
memoria.


        ================== =============================== ============
        Sequenza di escape Significato                     Codice ASCII
        ================== =============================== ============
        \\0                Byte nullo                      0
        \\t                Tabulazione orizzontale         9
        \\n                Carattere di inizio nuova linea 10
        \\"                Doppi apici                     34
        \\                 Backslash                       92
        ================== =============================== ============

La direttiva `.asciiz` si comporta esattamente come il comando `.ascii`, con
la differenza che essa pone automaticamente alla fine della stringa un byte
nullo.

La sezione `.code`
------------------
La sezione *code* contiene le istruzioni che saranno eseguite dal
simulatore a run-time. La forma generale di un comando `.code` è::

    [etichetta:] istruzione [param1 [, param2 [, param3]]]

Essa può essere indicata anche con la direttiva `.text`.

Il numero e il tipo di parametri dipendono dall'istruzione stessa.

.. %TODO: questa va sicuramente inserita.
   %Please see table~\ref{table:segm-type} for the list of possible parameters.

Le istruzioni possono accettare tre tipi di parametri:

* *Registri* un parametro di tipo registro è indicato da una
  "r" maiuscola o minuscola, o da un carattere "\$", a fianco del numero
  di registro (tra 0 e 31). Ad esempio, le scritture "r4", "R4" e "\$4"
  identificano tutt'e tre il quarto registro;
* *Valori immediati* un valore immediato può essere un numero o
  un'etichetta; il numero può essere specificato in base 10 o in base 16. I
  numeri in base 10 sono inseriti semplicemente scrivendo il numero
  utilizzando l'usuale notazione decimale; i numeri in base 16 si inseriscono
  aggiungendo all'inizio del numero il prefisso "0x";
* *Indirizzi* un indirizzo è composto da un valore immediato
  seguito dal nome di un registro tra parentesi. Il valore del registro sarà
  usato come base, quello dell'immediato come offset.

La dimensione dei valori immediati è limitata al numero di bit disponibili
nella codifica associata all'istruzione.

è possibile utilizzare gli alias standard MIPS per i primi 32 registri,
mettendo in coda ai prefissi standard per i registri ("r", "$", "R") uno
degli alias indicati nella seguente tabella.

            ======== ======
            Registro Alias
            ======== ======
            0        `zero`
            1        `at`
            2        `v0`
            3        `v1`
            4        `a0`
            5        `a1`
            6        `a2`
            7        `a3`
            8        `t0`
            9        `t1`
            10       `t2`
            11       `t3`
            12       `t4`
            13       `t5`
            14       `t6`
            15       `t7`
            16       `s0`
            17       `s1`
            18       `s2`
            19       `s3`
            20       `s4`
            21       `s5`
            22       `s6`
            23       `s7`
            24       `t8`
            25       `t9`
            26       `k0`
            27       `k1`
            28       `gp`
            29       `sp`
            30       `fp`
            31       `ra`
            ======== ======

.. % TODO: anche questa, ma nell'indice
   %Please see~\cite{mips-2} for more details about how instruction are
   actually encoded.

.. The instructions that can be used in this section will be discussed in
   section~\ref{instructions}

Il comando `\#include`
----------------------
Nei sorgenti può essere utilizzato il comando `*\#include* nomefile`, che ha
l'effetto di inserire, al posto della riga contenente questo comando, il
contenuto del file `nomefile`.
Questo comando è utile se si vogliono includere delle funzioni esterne, ed è
dotato di un algoritmo di rilevamento dei cicli, che impedisce di eseguire
inclusioni circolari tipo "`\#include A.s`" nel file `B.s` e
"`\#include B.s`" nel file `A.s`.
