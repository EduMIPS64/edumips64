Il set di istruzioni
====================

In questa sezione verrà illustrato il repertorio delle istruzioni MIPS64
riconosciute da EduMIPS64. è possibile effettuare due differenti
classificazioni: una basata sulla funzionalità delle istruzioni e l'altra
basata sul tipo di parametri.

.. Please refer to Section~\ref{mipsis} for more informations about those
   classifications.

La prima classificazione suddivide le istruzioni in tre categorie: istruzioni
ALU, istruzioni Load/Store, istruzioni di controllo del flusso. I prossimi tre
paragrafi descriveranno ciascuna categoria e le istruzioni che vi
appartengono.

Il quarto paragrafo descriverà le istruzioni che non rientrano in nessuna
delle tre categorie sopraelencate.

.. For a more complete MIPS64 instruction set reference, please refer
   to~\cite{mips-2}.


Le istruzioni ALU
-----------------
L'unità logico-aritmetica (ALU) fa parte dell'unità esecutiva di una
CPU ed assume il ruolo di esecuzione di operazioni logiche ed aritmetiche. Il
gruppo di istruzioni ALU conterrà quindi quelle istruzioni che effettuano
questo tipo di operazioni.

Le istruzioni ALU possono essere suddivise in due gruppi: *tipo R* e
*tipo I*.

Quattro di esse utilizzano due registri speciali: LO e HI. Tali registri sono
interni alla CPU ed è possibile accedere al loro valore mediante le
istruzioni `MFLO` e `MFHI`.

Ecco la lista delle istruzioni ALU di tipo R.

* `AND rd, rs, rt`

  Esegue un AND bit a bit tra rs ed rt, e pone il risultato in rd.

* `ADD rd, rs, rt`

  Somma il contenuto dei registri a 32-bit rs ed rt, considerandoli come
  valori con segno, e pone il risultato in rd.  Lancia un'eccezione in caso di
  overflow.


* `ADDU rd, rs, rt`

  Somma il contenuto dei registri a 32-bit rs ed rt, e pone il risultato in
  rd. Non si verificano eccezioni di overflow.

.. \MISN{}

* `DADD rd, rs, rt`

  Somma il contenuto dei registri a 64-bit rs ed rt, considerandoli come
  valori con segno, e pone il risultato in rd.  Lancia un'eccezione in caso di
  overflow.

* `DADDU rd, rs, rt`

  Somma il contenuto dei registri a 64-bit rs ed rt, e pone il risultato in rd.
  Non si verificano eccezioni di overflow.

.. \MISN{}

* `DDIV rs, rt`

  Esegue la divisione tra i registri a 64-bit rs ed rt, ponendo i 64-bit del
  quoziente in LO ed i 64-bit del resto in HI.

* `DDIVU rs, rt`

  Esegue la divisione tra i registri a 64-bit rs ed rt, considerandoli come
  valori senza segno e ponendo i 64-bit del quoziente in LO ed i 64-bit del
  resto in HI.

* `DIV rs, rt`

  Esegue la divisione tra i registri a 32-bit rs ed rt, ponendo i 32-bit del
  quoziente in LO ed i 32-bit del resto in HI.

* `DIVU rs, rt`

  Esegue la divisione tra i registri a 32-bit rs ed rt, considerandoli come
  valori senza segno e pone i 32-bit del quoziente in LO ed i 32-bit del resto
  in HI.

* `DMULT rs, rt`

  Esegue il prodotto tra i registri a 64-bit rs ed rt, ponendo i 64 bit bassi
  del risultato nel registro speciale LO e i 64 bit alti del risultato nel
  registro speciale HI.

* `DMULTU rs, rt`

  Esegue il prodotto tra i registri a 64-bit rs ed rt, considerandoli come
  valori senza segno e ponendo i 64 bit bassi del risultato nel registro
  speciale LO e i 64 bit alti del risultato nel registro speciale HI.

* `DMULU rs, rt`

  Esegue il prodotto tra i registri a 64-bit rs ed rt, considerandoli come
  valori senza segno e ponendo i 64 bit bassi del risultato nel registro
  speciale LO e i 64 bit alti del risultato nel registro speciale HI.

* `DSLL rd, rt, sa`

  Effettua uno shift verso sinistra del registro a 64-bit rt, di un numero di
  bit indicato nel valore immediato (positivo compreso tra 0 e 63) sa, e pone
  il risultato in rd. I bit liberi vengono posti a zero.

* `DSLLV rd, rt, rs`

  Effettua uno shift verso sinistra del registro a 64-bit rt, di un numero di
  bit specificato nei 6 bit bassi del registro rs che verrà letto come valore
  senza segno, e pone il risultato in rd. I bit liberi vengono posti a zero.

* `DSRA rd, rt, sa`

  Effettua uno shift verso destra del registro a 64-bit rt, di un numero di
  bit specificato nel valore senza segno immediato (positivo compreso tra 0 e
  63) sa, e pone il risultato in rd. I bit liberi vengono posti a zero se il
  bit più a sinistra di rs è zero, altrimenti vengono posti a uno.

* `DSRAV rd, rt, rs`

  Effettua uno shift verso destra del registro a 64-bit rt, di un numero di
  bit specificato nei 6 bit bassi del registro rs che verrà letto come valore
  senza segno,e pone il risultato in rd.  I bit liberi vengono posti a zero se
  il bit più a sinistra di rs è zero, altrimenti vengono posti a uno.

* `DSRL rd, rs, sa`

  Effettua uno shift verso destra del registro a 64-bit rt, di un numero di
  bit specificato nel valore immediato (positivo compreso tra 0 e 63) sa, e
  pone il risultato in rd. I bit liberi vengono posti a zero.

* `DSRLV rd, rt, rs`

  Effettua uno shift verso destra del registro a 64-bit rt, di un numero di
  bit specificato nei 6 bit bassi del registro rs che verrà letto come valore
  senza segno, e pone il risultato in rd. I bit liberi vengono posti a zero.

* `DSUB rd, rs, rt`

  Sottrae il valore del registro a 64-bit rt al valore del registro a 64-bit
  rs, considerandoli come valori con segno, e pone il risultato in rd. Lancia
  un'eccezione in caso di overflow.

* `DSUBU rd, rs, rt`

  Sottrae il valore del registro a 64-bit rt al valore del registro a 64-bit
  rs, e pone il risultato in rd.  Non si verificano eccezioni di overflow.

.. \MISN{}

* `MFLO rd`

  Copia il contenuto del registro speciale LO in rd.

* `MFHI rd`

  Copia il contenuto del registro speciale HI in rd.

* `MOVN rd, rs, rt`

  Se rt è diverso da zero, copia il contenuto di rs in rd.

* `MOVZ rd, rs, rt`

  Se rt è uguale a zero, copia il contenuto di rs in rd.

* `MULT rs, rt`

  Esegue il prodotto tra i registri a 32-bit rs ed rt, ponendo i 32 bit bassi
  del risultato nel registro speciale LO e i 32 bit alti del risultato nel
  registro speciale HI.

* `MULTU rs, rt`

  Esegue il prodotto tra i registri a 32-bit rs ed rt, considerandoli come
  valori senza segno e ponendo i 32 bit bassi del risultato nel registro
  speciale LO e i 32 bit alti del risultato nel registro speciale HI.

* `OR rd, rs, rt`

  Esegue un OR bit a bit tra rs ed rt, e pone il risultato in rd.

* `SLL rd, rt, sa`

  Effettua uno shift verso sinistra del registro a 32-bit rt, di un numero di
  bit indicati nel valore immediato (positivo compreso tra 0 e 63) sa, e pone
  il risultato nel registro a 32-bit rd. I bit liberi vengono posti a zero.

* `SLLV rd, rt, rs`

  Effettua uno shift verso sinistra del registro a 32-bit rt, di un numero di
  bit specificato nei 5 bit bassi del registro rs che verrà letto come
  valore senza segno, e pone il risultato nel registro a 32-bit rd. I bit
  liberi vengono posti a zero.

* `SRA rd, rt, sa`

  Effettua uno shift verso destra del registro a 32-bit rt, di un numero di
  bit specificato nel valore immediato (positivo compreso tra 0 e 63) sa, e
  pone il risultato nel registro a 32-bit rd.  I bit liberi vengono posti a
  zero se il bit più a sinistra di rs è zero, altrimenti vengono posti
  a uno.

* `SRAV rd, rt, rs`

  Effettua uno shift verso destra del registro a 32-bit rt, di un numero di
  bit specificato nei 5 bit bassi del registro rs che verrà letto come
  valore senza segno, e pone il risultato nel registro a 32-bit in rd.  I bit
  liberi vengono posti a zero se il bit più a sinistra di rs è zero,
  altrimenti vengono posti a uno.

* `SRL rd, rs, sa`

  Effettua uno shift verso destra del registro a 32-bit rt, di un numero di
  bit specificato nel valore immediato (positivo compreso tra 0 e 63) sa, e
  pone il risultato nel registro a 32-bit rd. I bit liberi vengono posti a
  zero.

* `SRLV rd, rt, rs`

  Effettua uno shift verso destra del registro a 32-bit rt, del numero di bit
  specificato nei 5 bit bassi del registro rs che verrà letto come valore
  senza segno, e pone il risultato nel registro a 32-bit rd. I bit liberi
  vengono posti a zero.

* `SUB rd, rs, rt`

  Sottrae il valore del registro a 32-bit rt al valore del registro a 32-bit
  rs, considerandoli come valori con segno, e pone il risultato in rd. Lancia
  un'eccezione in caso di overflow.

* `SUBU rd, rs, rt`

  Sottrae il valore del registro a 32-bit rt al valore del registro a 32-bit
  rs, e pone il risultato in rd.
  Non si verificano eccezioni di overflow.

.. \MISN{}

* `SLT rd, rs, rt`

  Pone il valore di rd ad 1 se il valore contenuto in rs è minore di
  quello contenuto in rt, altrimenti pone rd a 0. Questa istruzione esegue un
  confronto con segno.

* `SLTU rd, rs, rt`

  Pone il valore di rd ad 1 se il valore contenuto in rs è minore di
  quello contenuto in rt, altrimenti pone rd a 0. Questa istruzione esegue un
  confronto senza segno.

* `XOR rd, rs, rt`

  Esegue un OR esclusivo (XOR) bit a bit tra rs ed rt, e pone il risultato in
  rd.

Ecco la lista delle istruzioni ALU di tipo I.

* `ADDI rt, rs, immediate`

  Effettua la somma tra il registro a 32 bit rs ed il valore immediato,
  ponendo il risultato in rt.  Questa istruzione considera gli operandi come
  valori con segno.  Lancia un'eccezione in caso di overflow.

* `ADDIU rt, rs, immediate`

  Effettua la somma tra il registro a 32 bit rs ed il valore immediato,
  ponendo il risultato in rt.  Non si verificano eccezioni di overflow.

.. \MISN{}

* `ANDI rt, rs, immediate`

  Esegue un AND bit a bit tra rs ed il valore immediato, ponendo il risultato
  in rt.

* `DADDI rt, rs, immediate`

  Effettua la somma tra il registro a 64 bit rs ed il valore immediato,
  ponendo il risultato in rt.  Questa istruzione considera gli operandi come
  valori con segno.  Lancia un'eccezione in caso di overflow.

* `DADDIU rt, rs, immediate`

  Effettua la somma tra il registro a 64 bit rs ed il valore immediato,
  ponendo il risultato in rt.  Non si verificano eccezioni di overflow.

.. \MISN{}

* `DADDUI rt, rs, immediate`

  Effettua la somma tra il registro a 64 bit rs ed il valore immediato,
  ponendo il risultato in rt.  Non si verificano eccezioni di overflow.

.. \MISN{}
.. \WARN{}

* `LUI rt, immediate`

  Carica la costante definita dal valore immediato nella metà superiore dei 32
  bit inferiori di rt, effettuando l'estensione del segno sui 32 bit superiori
  del registro.

* `ORI rt, rs, immediate`

  Effettua l'OR bit a bit tra rs ed il valore immediato, ponendo il risultato
  in rt.

* `SLTI rt, rs, immediate`

  Pone il valore di rt ad 1 se il valore di rs è minore di quello
  dell'immediato, altrimenti pone rt a 0. Questa operazione effettua un
  confronto con segno.

* `SLTUI rt, rs, immediate`

  Pone il valore di rt ad 1 se il valore di rs è minore di quello
  dell'immediato, altrimenti pone rt a 0. Questa operazione effettua un
  confronto senza segno.

* `XORI rt, rs, immediate`

  Effettua l'OR esclusivo bit a bit tra rs ed il valore immediato, ponendo il
  risultato in rt.

Istruzioni load/store
---------------------
Questa categoria contiene tutte le istruzioni che effettuano trasferimenti di
dati tra i registri e la memoria. Ognuna di esse è espressa nella forma::

    [etichetta] istruzione rt, offset(base)

In base all'utilizzo di un'istruzione load oppure store, rt rappresenterà di
volta in volta il registro sorgente o destinazione; offset è un'etichetta o un
valore immediato e base è un registro.  L'indirizzo è ottenuto sommando al
valore del registro`base` il valore immediato di `offset`.

L'indirizzo specificato deve essere allineato in base al tipo di dato che si
sta trattando.  Le istruzioni di caricamento che terminano con "U" considerano
il contenuto del registro rt come un valore senza segno.

Ecco la lista delle istruzioni di caricamento (LOAD):

* `LB rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come byte con segno.

* `LBU rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come byte senza
  segno.

* `LD rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come una double
  word.

* `LH rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come una half word
  con segno.

* `LHU rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come una half word
  senza segno.

* `LW rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come una word con
  segno.

* `LWU rt, offset(base)`

  Carica il contenuto della cella di memoria all'indirizzo specificato da
  offset e base nel registro rt, considerando tale valore come una word senza
  segno.

Ecco la lista delle istruzioni di memorizzazione (STORE):

* `SB rt, offset(base)`

  Memorizza il contenuto del registro rt nella cella di memoria specificata da
  offset e base, considerando tale valore come un byte.

* `SD rt, offset(base)`

  Memorizza il contenuto del registro rt nella cella di memoria specificata da
  offset e base, considerando tale valore come una double word.

* `SH rt, offset(base)`

  Memorizza il contenuto del registro rt nella cella di memoria specificata da
  offset e base, considerando tale valore come una half word.

* `SW rt, offset(base)`

  Memorizza il contenuto del registro rt nella cella di memoria specificata da
  offset e base, considerando tale valore come una word.

Istruzioni di controllo del flusso
----------------------------------
Le istruzioni di controllo del flusso sono utilizzate per alterare l'ordine
delle istruzioni prelevate dalla CPU nella fase di fetch. è possibile fare una
distinzione tra tali istruzioni: tipo R, tipo I e tipo J.

Tali istruzioni eseguono il salto alla fase di Instruction Decode (ID), ogni
qual volta viene effettuato un fetch inutile. In tal caso, due istruzioni
vengono rimosse dalla pipeline, ed il contatore degli stalli dovuti ai salti
effettuati viene incrementato di due unità.

Ecco la lista delle istruzioni di controllo del flusso di tipo R:

* `JALR rs`

  Pone il contenuto di rs nel program counter, e salva in R31 l'indirizzo
  dell'istruzione che segue l'istruzione JALR, che rappresenta il valore di
  ritorno.

* `JR rs`

  Pone il contenuto di rs nel program counter.

Ed ecco le istruzioni di controllo del flusso di tipo I:

* `B offset`

  Salto incondizionato ad offset.

* `BEQ rs, rt, offset`

  Salta ad offset se rs è uguale ad rt.

* `BEQZ rs, offset`

  Salta ad offset se rs è uguale a zero.

..  \WARN

* `BGEZ rs, offset`

  Effettua un salto relativo al PC ad offset se rs è maggiore di zero.

* `BNE rs, rt, offset`

  Salta ad offset se rs non è uguale ad rt.

* `BNEZ rs, offset`

  Salta ad offset se rs non è uguale a zero.

..  \WARN

Ecco la lista delle istruzioni di controllo del flusso di tipo J:

* `J target`

  Pone il valore immediato nel program counter

* `JAL target`

  Pone il valore immediato nel program counter, e salva in R31 l'indirizzo
  dell'istruzione che segue l'istruzione JAL, che rappresenta il valore di
  ritorno.

L'istruzione `SYSCALL`
----------------------
L'istruzione SYSCALL offre al programmatore un'interfaccia simile a quella
offerta da un sistema operativo, rendendo disponibili sei differenti chiamate
di sistema (system call).

Le system call richiedono che l'indirizzo dei loro parametri sia memorizzato
nel registro R14 ($t6), e pongono il loro valore di ritorno nel registro R1
($at). Tali system call sono il più possibile fedeli alla convenzione POSIX.

`SYSCALL 0 - exit()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 0 non richiede alcun parametro nè ritorna nulla, semplicemente ferma
il simulatore.

è opportuno notare che se il simulatore non trova SYSCALL 0 nel codice
sorgente, o una qualsiasi istruzione equivalente (HALT  TRAP 0), terminerà
automaticamente alla fine del sorgente.

`SYSCALL 1 - open()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 1 richiede due parametri: una stringa (che termini con valore zero) che
indica il percorso del file che deve essere aperto, ed una double word
contenente un intero che indica i parametri che devono essere usati per
specificare come aprire il file.

Tale intero può essere costruito sommando i parametri che si vogliono
utilizzare, scelti dalla seguente lista:

* `O_RDONLY (0x01)` Apre il file in modalità sola lettura;
* `O_WRONLY (0x02)` Apre il file in modalità sola scrittura;
* `O_RDWR (0x03)` Apre il file in modalità di lettura/scrittura;
* `O_CREAT (0x04)` Crea il file se non esiste;
* `O_APPEND (0x08)` In modalità di scrittura, aggiunge il testo alla fine del
  file;
* `O_TRUNC (0x08)` In modalità di scrittura, cancella il contenuto del file al
  momento della sua apertura.

È obbligatorio specificare una delle prime tre modalità. La quinta e
la sesta sono esclusive, non è possibile specificare O_APPEND se si
specifica O_TRUNC (e viceversa).Inoltre non si puo' specificare O_CREAT se
si specifica O_RDONLY (oppure O_RDWR).

È possibile specificare una combinazione di modalità semplicemente
sommando i valori interi ad esse associati.  Ad esempio, se si vuole aprire un
file in modalità di sola scrittura ed aggiungere il testo alla fine del
file, si dovrà specificare la modalità 2 + 8 = 10.

Il valore di ritorno delle chiamate di sistema è il nuovo descrittore del
file (file descriptor) associato al file, che potrà essere utilizzato con
le altre chiamate di sistema. Qualora si verifichi un errore, il valore di
ritorno sarà -1.

`SYSCALL 2 - close()`
~~~~~~~~~~~~~~~~~~~~~
SYSCALL 2 richiede solo un parametro, il file descriptor del file che deve
essere chiuso.

Qualora l'operazione termini con successo, SYSCALL 2 ritornerà 0, altrimenti
-1.  Possibili cause di errore sono il tentativo di chiudere un file
inesistente, o di chiudere i file descriptor 0, 1 o 2, che sono associati
rispettivamente allo standard input, allo standard output ed allo standard
error.

`SYSCALL 3 - read()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 3 richiede tre parametri: il file descriptor da cui leggere,
l'indirizzo nel quale i dati letti dovranno essere copiati, il numero di byte
da leggere.

Se il primo parametro è 0, il simulatore permetterà all'utente di
inserire un valore mediante un'apposita finestra di dialogo.  Se la lunghezza
del valore immesso è maggiore del numero di byte che devono essere letti,
il simulatore mostrerà nuovamente la finestra.

La chiamata di sistema ritorna il numero di byte effettivamente letti, o -1 se
l'operazione di lettura fallisce. Possibili cause di errore sono il tentativo
di leggere da un file inesistente, o di leggere dai file descriptor 1
(standard output) o 2 (standard error), oppure il tentativo di leggere da un
file di sola scrittura.

`SYSCALL 4 - write()`
~~~~~~~~~~~~~~~~~~~~~
SYSCALL 4 richiede tre parametri: il file descriptor su cui scrivere,
l'indirizzo dal quale i dati dovranno essere letti, il numero di byte da
scrivere.

Se il primo parametro è 2 o 3, il simulatore mostrerà la finestra di
input/output dove scriverà i dati letti.

Questa chiamata di sistema ritorna il numero di byte che sono stati scritti, o
-1 se l'operazione di scrittura fallisce.  Possibili cause di errore sono il
tentativo di scrivere su un file inesistente, o sul file descriptor 0
(standard input), oppure il tentativo di scrivere su un file di sola lettura.

`SYSCALL 5 - printf()`
~~~~~~~~~~~~~~~~~~~~~~
SYSCALL 5 richiede un numero variabile di parametri, il primo è la
cosiddetta "format string" o stringa di formato. Nella stringa di formato
possono essere inseriti alcuni segnaposto, descritti nella seguente lista:

* `%s` parametro di tipo stringa;
* `%i` parametro di tipo intero;
* `%d` si comporta come `%i`;
* `%%` carattere `%`

Per ciascuno dei segnaposto `\%s`, `\%d` o `\%i` la SYSCALL 5
si aspetta un parametro, partendo dall'indirizzo del precedente.

Quando la SYSCALL trova un segnaposto per un parametro intero, si aspetta che
il corrispondente parametro sia un valore intero, quando trova un segnaposto
per un parametro stringa, si aspetta come parametro l'indirizzo della stringa
stessa.

Il risultato  visualizzato nella finestra di input/output, ed il numero di
byte scritti posto in R1.

Qualora si verifichi un errore, R1 avrà valore -1.

Altre istruzioni
----------------
In questa sezione sono descritte istruzioni che non rientrano nelle precedenti
categorie.

`BREAK`
~~~~~~~
L'istruzione BREAK solleva un'eccezione che ha l'effetto di fermare
l'esecuzione se il simulatore è in esecuzione. Può essere utilizzata per
il debugging.

`NOP`
~~~~~
L'istruzione NOP non fa nulla, ed è utilizzata per creare pause nel codice
sorgente.

`TRAP`
~~~~~~
L'istruzione TRAP è deprecated, rappresenta un'alternativa all'istruzione
SYSCALL.

`HALT`
~~~~~~
L'istruzione HALT è deprecated, rappresenta un'alternativa all'istruzione
SYSCALL 0, che ferma il simulatore.
