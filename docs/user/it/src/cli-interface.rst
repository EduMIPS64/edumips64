L'interfaccia a linea di comando interattiva
============================================
Oltre alla GUI desktop, il JAR di EduMIPS64 può essere eseguito anche
come shell interattiva a linea di comando. La shell è pensata per la
sperimentazione "batch" su programmi assembly (esecuzione di test in
script, materiale didattico, valutazione automatica, debug da
terminale, ecc.) ed offre lo stesso core simulatore della GUI desktop,
con la sola differenza di un'interfaccia testuale.

Questo capitolo descrive i *comandi* disponibili all'interno della
shell. Le *opzioni* da linea di comando per avviare il JAR
(``--headless``, ``--file``, ``--verbose``, …) sono documentate in
:ref:`command-line-options-it`; in particolare, la shell si raggiunge
avviando il JAR con ``--headless`` (con o senza ``--verbose``).

Quando ``--verbose`` è abilitato, la shell stampa messaggi
informativi aggiuntivi — un banner di benvenuto all'avvio, messaggi di
"esecuzione iniziata" / "esecuzione terminata" attorno a ``run``,
puntini di progresso durante esecuzioni lunghe, eventuali warning del
parser dopo ``load``, ecc. Senza ``--verbose`` la shell produce solo
l'output del programma (ad esempio le stringhe stampate da
``SYSCALL 5``) e le risposte esplicite di ``show``, ``config`` ed
altri comandi. Il default silenzioso è comodo quando EduMIPS64 viene
inserito in pipeline con altri strumenti.

Il prompt
---------
All'avvio la shell stampa un prompt ``>`` ed attende un comando. I
comandi sono letti una riga per volta e suddivisi in token in
corrispondenza degli spazi. Premendo Invio su una riga vuota viene
ristampato l'help. Il loop della shell continua fino al comando
``exit`` (o fino a ``Ctrl+D`` / EOF su standard input).

Ogni comando accetta ``-h`` / ``--help``, che ne stampa l'uso
specifico. Il comando di livello superiore ``help`` elenca tutti i
comandi disponibili con una breve descrizione; è il modo più semplice
per scoprire cosa la shell può fare.

Comandi disponibili
-------------------
La shell espone un piccolo insieme di comandi, che mappano direttamente
sul ciclo di vita del simulatore: caricare un file sorgente,
eseguirlo passo passo o tutto d'un fiato, ispezionare lo stato
risultante e, opzionalmente, generare un tracefile per Dinero per
l'analisi della cache.

load
~~~~
Carica un nuovo file da eseguire::

    > load percorso/al/programma.s

``load`` analizza il file indicato e prepara il simulatore
all'esecuzione. Se il caricamento ha successo la CPU passa nello stato
``RUNNING`` ed è pronta per essere fatta avanzare con ``step`` o
``run``.

Se il parser segnala errori il file non viene caricato e viene
stampata la descrizione degli errori. Se invece vengono prodotti solo
warning, il file viene caricato comunque — coerentemente con il
comportamento della GUI; con ``--verbose`` i warning vengono stampati
a schermo.

Non è possibile caricare un nuovo programma mentre uno precedente è
ancora in esecuzione; usa prima ``reset`` per riportare la CPU in uno
stato in cui sia possibile caricare un nuovo file.

step
~~~~
Fa avanzare la macchina a stati della CPU di N cicli::

    > step          # avanza di 1 ciclo (default)
    > step 10       # avanza di 10 cicli

Dopo ogni ciclo viene stampato il contenuto della pipeline, quindi
``step`` è il modo canonico per seguire l'esecuzione un'istruzione
alla volta ed osservare come le istruzioni attraversino gli stadi IF
/ ID / EX / MEM / WB e gli stadi della FPU.

Se il programma termina (``SYSCALL 0`` o ``BREAK``) prima del numero
richiesto di cicli, ``step`` si interrompe a quel punto e stampa il
messaggio corrispondente.

run
~~~
Esegue il programma senza interventi::

    > run

Il simulatore avanza finché il programma non termina con
``SYSCALL 0`` (o equivalente) o con un'istruzione ``BREAK``. Con
``--verbose``, la shell stampa banner di *inizio* / *fine*, un puntino
di progresso ogni mille cicli ed un riepilogo finale con il numero
totale di cicli eseguiti ed il tempo wall-clock impiegato. Usa
``run`` per ottenere rapidamente l'output del programma
(``SYSCALL 4`` / ``SYSCALL 5``) e lo stato finale, e poi ``show`` per
ispezionare il risultato.

show
~~~~
Ispeziona lo stato della CPU simulata. ``show`` è un gruppo di
sotto-comandi; ognuno stampa su standard output un aspetto diverso
dello stato del simulatore.

* ``show registers`` — stampa tutti i 32 registri general-purpose
  interi (``R0``–``R31``) con il loro valore corrente.
* ``show register N`` — stampa il contenuto del registro intero
  ``N`` (``0 ≤ N ≤ 31``).
* ``show fps`` — stampa tutti i 32 registri in virgola mobile
  (``F0``–``F31``).
* ``show fp N`` — stampa il contenuto del registro in virgola mobile
  ``N`` (``0 ≤ N ≤ 31``).
* ``show fcsr`` — stampa il Floating-Point Control and Status
  Register.
* ``show hi`` / ``show lo`` — stampano il contenuto dei registri
  speciali ``HI`` e ``LO`` usati dalle istruzioni di moltiplicazione
  e divisione.
* ``show memory`` — stampa il contenuto della memoria principale
  simulata.
* ``show symbols`` — stampa la tabella dei simboli (le label
  dichiarate nelle sezioni ``.data`` e ``.code`` con i relativi
  indirizzi).
* ``show pipeline`` — stampa quale istruzione si trovi attualmente in
  ciascuno stadio della pipeline.

Invocando ``show`` senza sotto-comando viene stampato l'elenco dei
sotto-comandi disponibili.

dinero
~~~~~~
Scrive un tracefile Dinero su un file::

    > dinero trace.xdin

Viene generata una traccia testuale degli accessi in memoria
effettuati dal programma nel formato atteso dal simulatore di cache
Dinero IV, adatto ad un'analisi offline della cache. ``dinero`` può
essere invocato in qualsiasi momento dell'esecuzione; il tracefile
riflette gli accessi osservati fino a quel punto.

config
~~~~~~
Stampa i valori correnti di configurazione::

    > config

È lo stesso insieme di preferenze che la GUI desktop espone nella
finestra *Settings* (forwarding on/off, percorso del tracefile,
parametri della cache, opzioni comportamentali, …). È in sola
lettura — la shell stampa i valori correnti ed esce. I valori
provengono dallo stesso store di configurazione usato dalla GUI,
quindi le modifiche fatte dalla GUI sono visibili dalla shell e
viceversa.

reset
~~~~~
Resetta la macchina a stati della CPU::

    > reset

``reset`` re-inizializza memoria, registri, tabella dei simboli,
gestore di I/O, simulatore di cache e parser, riportando la CPU allo
stato ``READY``. Usa ``reset`` per caricare un programma diverso dopo
quello corrente, oppure per ricominciare un programma da zero dopo
un'esecuzione parziale.

help
~~~~
Mostra l'elenco dei comandi disponibili con una breve descrizione di
ciascuno. ``help`` è il punto di partenza per scoprire le funzionalità
della shell; combinalo con l'opzione ``-h`` / ``--help`` di ogni
comando per vedere i parametri accettati dai singoli sotto-comandi.

exit
~~~~
Esce dalla shell e termina la JVM. Lo stesso effetto si ottiene
inviando ``Ctrl+D`` (EOF) su standard input.

Una sessione tipica
-------------------
Mettendo insieme i comandi, una tipica sessione interattiva ha questo
aspetto::

    $ java -jar edumips64.jar --headless --verbose
    > load examples/hello.s
    File loaded: /…/examples/hello.s
    > step 5
    … (5 cicli di contenuto della pipeline)
    > run
    Hello, world!
    Execution finished in 42 cycles, 3 ms
    > show registers
    … (R0..R31)
    > show pipeline
    … (stato finale della pipeline)
    > dinero hello.xdin
    > reset
    > load examples/sum.s
    > run
    > exit

Lo standard input da ``SYSCALL 3`` viene letto direttamente dal
terminale, quindi i programmi che richiedono input utente funzionano
in modo trasparente nella shell.

Suggerimenti per gli script
---------------------------
Dato che la shell legge i comandi da standard input uno per riga, è
possibile darle in pasto direttamente uno script::

    $ java -jar edumips64.jar --headless < session.txt

Un ``session.txt`` contenente ad esempio::

    load examples/hello.s
    run
    show registers
    exit

caricherà il programma, lo eseguirà, stamperà il register file finale
e terminerà. In combinazione con ``--verbose`` e con la
redirezione della shell di sistema, questo rende immediato integrare
EduMIPS64 in suite di test automatiche o catturare trace
riproducibili di un'esecuzione.
