La pipeline e il forwarding
===========================

EduMIPS64 modella la classica pipeline intera MIPS a 5 stadi descritta
nell'Appendice C (*Pipelining: Basic and Intermediate Concepts*) di Hennessy
e Patterson, *Computer Architecture: A Quantitative Approach* (nelle edizioni
più vecchie la stessa appendice è numerata "A"). Gli stadi sono:

* **IF** — Instruction Fetch (prelievo dell'istruzione)
* **ID** — Instruction Decode (decodifica e lettura dei registri)
* **EX** — Execute (esecuzione / calcolo dell'indirizzo effettivo)
* **MEM** — Memory access (accesso alla memoria)
* **WB** — Write-Back (scrittura del risultato)

In ogni ciclo di clock possono essere presenti nella pipeline fino a cinque
istruzioni, una per stadio.

Conflitti sui dati (data hazard)
--------------------------------
Un conflitto di tipo *Read-After-Write* (RAW) si verifica quando
un'istruzione prova a leggere un registro prima che un'istruzione precedente,
ancora in esecuzione nella pipeline, ne abbia scritto il risultato. EduMIPS64
individua i conflitti RAW nello stadio **ID** dell'istruzione consumatrice,
controllando un semaforo di scrittura per-registro che viene attivato quando
l'istruzione produttrice "blocca" il suo registro di destinazione (sempre in
ID) e rilasciato quando il valore è finalmente disponibile. Quando viene
rilevato un conflitto la pipeline inserisce una o più *bolle* in EX e le
conta come **stalli RAW** nel pannello delle statistiche.

Un conflitto di tipo *Write-After-Write* (WAW) può verificarsi soltanto
sulla FPU, poiché la pipeline FPU non è uniforme (le diverse unità funzionali
hanno latenze diverse) e due istruzioni possono raggiungere lo stadio WB in
ordine diverso rispetto a quello di programma. I conflitti WAW vengono
rilevati in ID e contati separatamente come **stalli WAW**.

Forwarding
----------
Il forwarding (detto anche *bypassing*) è una tecnica hardware che rende il
risultato di un'istruzione disponibile alle istruzioni dipendenti prima che
venga scritto nel banco dei registri, riducendo il numero di stalli causati
dai conflitti RAW. È la tecnica standard descritta in Hennessy e Patterson,
Appendice C, Sezione C.2 ("The Major Hurdle of Pipelining — Pipeline
Hazards"), nel paragrafo "Minimizing Data Hazard Stalls by Forwarding".

In EduMIPS64 il forwarding può essere abilitato o disabilitato dalla scheda
*Impostazioni generali* della finestra *Impostazioni* (si veda
:doc:`user-interface`). Quando è attivo, il simulatore inoltra il valore
direttamente dallo stadio produttore (uscita di EX, uscita di MEM o WB) allo
stadio consumatore (tipicamente lo stadio EX dell'istruzione successiva, o
lo stadio ID di un salto), purché il produttore sia già abbastanza avanti
nella pipeline. Quando è disattivato, ogni dipendenza RAW deve attendere il
completamento dello stadio WB del produttore prima che il consumatore possa
leggere il valore in ID.

I due comportamenti si possono confrontare caricando lo stesso programma due
volte, una con il forwarding attivo e una con il forwarding disattivo, e
osservando il contatore dei cicli e quello degli stalli RAW nel pannello
*Statistiche*.

Dipendenze ALU-ALU
~~~~~~~~~~~~~~~~~~
Si consideri l'esempio canonico di Hennessy e Patterson, Appendice C,
Figura C.5 (all'incirca pagina C-16):

.. code-block:: text

    DADD  R1, R2, R3
    DSUB  R4, R1, R5
    AND   R6, R1, R7
    OR    R8, R1, R9
    XOR   R10, R1, R11

Tutte e quattro le istruzioni successive a ``DADD`` dipendono da ``R1``.

* **Senza forwarding**: ``DSUB`` dovrebbe rimanere in stallo in ID finché
  ``DADD`` non ha scritto ``R1`` in WB, cioè per due cicli. ``AND`` dovrebbe
  stallare per un ciclo. ``OR`` invece non stalla solo perché si assume che
  il banco dei registri venga scritto nella prima metà del ciclo di clock e
  letto nella seconda metà (equivalente a un forwarding "interno" al banco
  dei registri).
* **Con forwarding**: il valore di ``R1`` calcolato alla fine dello stadio
  EX di ``DADD`` viene inoltrato direttamente allo stadio EX di ``DSUB`` (e
  di ``AND``, ``OR``, ``XOR``). Per le catene ALU-ALU non è necessario alcun
  stallo.

Questo esempio è presente come fixture di test col nome
``forwarding-hp-pA16.s``.

Dipendenze load-use
~~~~~~~~~~~~~~~~~~~
Il forwarding non elimina tutti gli stalli. Si consideri (Hennessy e
Patterson, Appendice C, Figura C.7, all'incirca pagina C-18):

.. code-block:: text

    DADD R1, R2, R3
    LD   R4, 0(R1)
    SD   R4, 8(R1)

``LD`` produce ``R4`` al termine dello stadio MEM, ma ``SD`` avrebbe bisogno
di ``R4`` all'inizio del proprio stadio MEM. Anche con il forwarding attivo
è necessario inserire una bolla tra il load e l'istruzione dipendente. È il
classico conflitto *load-use*, visibile in EduMIPS64 come un singolo stallo
RAW quando il forwarding è abilitato. Questo esempio è presente come fixture
di test col nome ``forwarding-hp-pA18.s``.

Salti e lo stadio ID
~~~~~~~~~~~~~~~~~~~~
La pipeline intera MIPS risolve i salti condizionali nello stadio **ID**
(si veda Hennessy e Patterson, Appendice C, Sezione C.2, paragrafo "Branch
Hazards"), confrontando i registri sorgente non appena vengono letti. Questo
ha una conseguenza importante sui conflitti RAW che coinvolgono i salti:

.. code-block:: text

    SLT   R1, R2, R4
    BEQZ  R1, finish

``BEQZ`` legge ``R1`` nel proprio stadio ID, che si svolge nello stesso
ciclo di clock dello stadio EX di ``SLT``. Il forwarding da EX a ID **non**
è possibile, perché il risultato dell'ALU è disponibile alla *fine* dello
stadio EX, che è anche il momento in cui ID termina. Di conseguenza **è
richiesto almeno uno stallo** tra un'istruzione ALU e un salto che dipende
dal suo risultato, anche con il forwarding abilitato. Un load seguito da un
salto dipendente richiede due stalli per lo stesso motivo.

EduMIPS64 individua questi casi e li conta nel contatore degli stalli RAW.

Ulteriori approfondimenti
-------------------------
La trattazione di riferimento per i conflitti sui dati, il forwarding e i
conflitti sui salti nella pipeline MIPS a 5 stadi è l'Appendice C di:

   John L. Hennessy e David A. Patterson, *Computer Architecture: A
   Quantitative Approach*, Morgan Kaufmann. (La stessa appendice è
   numerata "A" nelle prime edizioni del libro.)

I programmi di test ``forwarding.s``, ``forwarding-hp-pA16.s`` e
``forwarding-hp-pA18.s`` contenuti in ``src/test/resources/`` riproducono
gli esempi di quell'appendice e sono un buon punto di partenza per
sperimentare con il simulatore.
