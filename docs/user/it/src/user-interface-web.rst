L'interfaccia utente web
========================
EduMIPS64 è disponibile anche come applicazione web che gira
interamente nel browser. Il core del simulatore è cross-compilato da
Java a JavaScript ed è eseguito come Web Worker, mentre l'interfaccia
utente è realizzata con React. Il deployment di produzione è ospitato
su https://web.edumips.org.

Questo capitolo descrive il frontend web. Per il formato dei file
sorgente, l'insieme di istruzioni supportato, la FPU e gli esempi di
programmi, fai riferimento agli altri capitoli del manuale: quei
contenuti sono indipendenti dall'interfaccia utente.

Panoramica del layout
---------------------
La finestra è suddivisa in una barra degli strumenti superiore e due
aree principali:

* a sinistra, l'**editor di codice** (un editor MIPS64 basato su
  Monaco);
* a destra, una pila di pannelli a fisarmonica che mostrano lo stato
  della simulazione: **Issues**, **Statistics**, **Pipeline**,
  **Registers**, **Memory**, **Standard Output**, **Cache
  Configuration** e **General Settings**.

La barra degli strumenti
------------------------
La barra in alto raccoglie tutte le azioni che controllano il
simulatore. Ogni pulsante ha un tooltip che ne descrive l'effetto.

* **Logo EduMIPS64 ed etichetta "Web Version"** — indicano la build
  corrente. Una piccola etichetta colorata viene mostrata quando la
  build non è quella di produzione:

  * un'etichetta gialla ``PR #N`` identifica una build di anteprima
    associata ad una pull request e contiene il link alla PR su
    GitHub;
  * un'etichetta blu ``dev`` identifica una build di sviluppo locale.

* **Indicatore di stato della CPU** — mostra lo stato corrente della
  CPU simulata, con un codice colore:

  * ``READY`` (verde) — nessun programma caricato, oppure la CPU è
    appena stata resettata;
  * ``RUNNING`` (giallo) — un programma è caricato e la CPU sta
    eseguendo cicli;
  * ``STOPPING`` / ``STOPPED`` (rosso) — è stata letta un'istruzione
    di terminazione e la pipeline si sta svuotando, oppure il
    programma è terminato.

* **Load** — analizza il contenuto dell'editor e carica il programma
  risultante nel simulatore. È disabilitato mentre la simulazione è
  in corso e viene nascosto una volta che il caricamento è andato a
  buon fine.

* **Single Step** — esegue un singolo ciclo di CPU.

* **Multi Step** — esegue un numero configurabile di cicli di CPU con
  un singolo click. Il numero corrente è mostrato nel tooltip del
  pulsante e può essere modificato nel pannello *General Settings*
  ("Multi Step Size").

* **Run All** — esegue il programma fino al termine, ovvero fino ad
  una ``SYSCALL 0`` (o equivalente) o ad una istruzione ``BREAK``,
  oppure fino a quando l'utente preme *Pause* o *Stop*. Tra un blocco
  interno di cicli e l'altro il simulatore può attendere un ritardo
  configurabile (``Execution Delay``), così da rendere visivamente
  osservabili anche esecuzioni lunghe.

* **Pause** — interrompe l'esecuzione in corso al ciclo corrente. È
  poi possibile proseguire con *Single Step*, *Multi Step* o *Run
  All*.

* **Stop** — interrompe l'esecuzione e riporta la CPU allo stato
  ``READY``, azzerando registri, memoria e pipeline.

* **Clear** — svuota l'editor lasciando solo uno scheletro di file
  assembly (direttive ``.data`` e ``.code`` ed una ``SYSCALL 0``
  finale). È disabilitato mentre la CPU sta eseguendo.

* **Open Code** — apre un file locale (tipicamente un file ``.s``) e
  ne carica il contenuto nell'editor.

* **Save Code** — salva il contenuto corrente dell'editor in un file
  locale chiamato ``code.s``.

* **Help (?)** — apre questo manuale all'interno dell'applicazione,
  con un pannello di navigazione sulla sinistra ed un selettore della
  lingua. La finestra di Help include anche una scheda *About* che
  mostra la versione del simulatore ed una descrizione della build in
  esecuzione.

I pulsanti che non avrebbero alcun effetto nello stato corrente sono
disabilitati automaticamente. Ad esempio, *Single Step*, *Multi Step*
e *Run All* sono disabilitati finché un programma non è stato
caricato con *Load*, e *Pause* è disponibile solo durante una
esecuzione lunga.

L'editor di codice
------------------
L'editor di codice è basato su `Monaco
<https://microsoft.github.io/monaco-editor/>`_ — lo stesso editor di
Visual Studio Code — ed è dedicato alla scrittura di assembly MIPS64.
Supporta tutte le funzionalità tipiche di un editor (cursori
multipli, find & replace, numeri di riga, undo/redo, layout
automatico, tema chiaro/scuro automatico in base alle preferenze del
sistema operativo) ed un certo numero di funzionalità specifiche di
EduMIPS64 descritte di seguito.

Evidenziazione della sintassi
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
L'editor evidenzia la sintassi dei sorgenti MIPS64:

* le label (righe che iniziano con un identificatore seguito da
  ``:``);
* tutte le istruzioni supportate da EduMIPS64 (l'elenco è calcolato a
  runtime a partire dal core del simulatore);
* le direttive che iniziano con ``.``, ad esempio ``.data``,
  ``.code``, ``.word``;
* i nomi dei registri della forma ``rNN``;
* le costanti numeriche;
* le stringhe;
* i commenti che iniziano con ``;``.

Validazione live
~~~~~~~~~~~~~~~~
Il simulatore analizza il codice in background mentre lo si scrive e
segnala eventuali **errori** ed **avvisi** direttamente nell'editor:

* gli errori sono sottolineati in rosso;
* gli avvisi sono sottolineati con un riccio giallo;
* passando il mouse sopra un'area sottolineata viene mostrato un
  tooltip con la descrizione del problema;
* la riga interessata è inoltre evidenziata nel margine dell'editor.

Gli stessi problemi sono riassunti nel pannello **Issues** sulla
destra (vedi sotto). Gli avvisi non bloccano l'esecuzione, gli errori
sì: premendo *Load* in presenza di errori comparirà una finestra di
notifica con il messaggio del parser.

Informazioni al passaggio del mouse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Una volta caricato il programma con *Load*, passando il mouse sopra
un'istruzione nell'editor compare un tooltip con informazioni
dettagliate:

* **Address** — l'indirizzo di memoria assegnato all'istruzione;
* **OpCode** — l'opcode assembly (ad esempio ``DADD``, ``LD``);
* **Binary** — la codifica binaria a 32 bit;
* **Hex** — la stessa codifica in esadecimale, riempita a sinistra
  con zeri fino a 8 cifre;
* **CPU Stage** — mostrato solo se l'istruzione è attualmente in
  pipeline; identifica lo stadio in cui si trova nel ciclo corrente
  (ad esempio ``Instruction Fetch (IF)``, ``Execute (EX)``, ``FPU
  Multiplier (3)``).

Visualizzazione in tempo reale degli stadi della pipeline
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Durante l'esecuzione, la riga di sorgente corrispondente all'istruzione
attualmente presente in ciascuno stadio della pipeline viene
evidenziata con un colore che identifica lo stadio. Il codice colore
è condiviso con il pannello **Pipeline**:

================================== ==================
Stadio                             Colore
================================== ==================
Instruction Fetch (IF)             Giallo
Instruction Decode (ID)            Blu
Execute (EX)                       Rosso
Memory Access (MEM)                Verde
Write Back (WB)                    Magenta
FPU Adder (1..4)                   Verde scuro
FPU Multiplier (1..7)              Acquamarina
FPU Divider                        Oliva
================================== ==================

L'evidenziazione segue le istruzioni attraverso la pipeline mentre la
simulazione avanza, fornendo un colpo d'occhio immediato su quali
righe di codice sono attive in quale stadio in ogni ciclo. Combinata
con il tooltip al passaggio del mouse descritto sopra, rende
semplice ispezionare lo stato della pipeline in un qualsiasi punto
dell'esecuzione.

L'editor diventa di sola lettura quando un programma è caricato nel
simulatore. Per modificare nuovamente il codice usa *Stop* per
resettare la CPU.

Salvataggio e caricamento
~~~~~~~~~~~~~~~~~~~~~~~~~
Il contenuto dell'editor può essere salvato e ripreso usando i
pulsanti **Save Code** e **Open Code** della barra degli strumenti.
*Save Code* avvia il download del sorgente come ``code.s``; *Open
Code* permette di selezionare un file locale, sostituendo il
contenuto dell'editor.

Modalità Vi opzionale
~~~~~~~~~~~~~~~~~~~~~
Una *modalità Vi* per l'editor può essere attivata dal pannello
*General Settings*. Una volta abilitata, l'editor riconosce le
combinazioni di tasti vi di base (modi, motion, ricerca), il che
torna utile per chi è abituato ad editare sorgenti da terminale.

Dimensione del font
~~~~~~~~~~~~~~~~~~~
La dimensione del font dell'editor può essere aumentata o diminuita
dal pannello *General Settings*; lo stesso valore viene utilizzato
anche dagli altri elementi monospaziati dell'interfaccia.

Il pannello Issues
------------------
Il pannello **Issues** sulla destra rispecchia la diagnostica che
l'editor mostra inline:

* ogni voce contiene la riga e la colonna del problema, oltre alla
  descrizione fornita dal parser;
* un'icona di avviso (triangolo giallo) marca gli avvisi, un'icona di
  errore (cerchio rosso) marca gli errori;
* l'intestazione del pannello mostra due piccoli contatori, uno per
  gli avvisi ed uno per gli errori. I contatori sono nascosti quando
  non c'è nulla da segnalare.

Il pannello Issues è espanso di default, in modo che i problemi siano
sempre visibili.

Pannelli a runtime
------------------
Il lato destro della finestra contiene una serie di pannelli a
fisarmonica indipendenti. Ogni pannello può essere espanso o chiuso
indipendentemente cliccando sulla sua intestazione; lo stato di
espansione viene mantenuto tra una sessione e l'altra.

Quando un pannello è chiuso e il suo contenuto cambia in seguito ad
uno step della simulazione, accanto al titolo del pannello compare un
piccolo pallino pulsante. In questo modo si può notare al volo un
cambiamento interessante (ad esempio una scrittura in un registro)
senza dover tenere espansi tutti i pannelli. L'indicatore pulsante
può essere disabilitato dalle *General Settings* ("Accordion Change
Alerts").

Statistics
~~~~~~~~~~
Contatori sull'esecuzione del programma:

* numero di cicli eseguiti;
* numero di istruzioni eseguite;
* CPI — cicli per istruzione (``cicli / istruzioni``);
* stalli RAW, WAW e strutturali;
* letture e miss della L1 istruzioni;
* letture, miss in lettura, scritture e miss in scrittura della L1
  dati (significativi solo se la cache è opportunamente configurata —
  vedi *Cache Configuration*).

Pipeline
~~~~~~~~
Mostra una rappresentazione grafica della pipeline della CPU che ricalca
il diagramma del classico front-end Swing. I cinque stadi interi
(IF, ID, EX, MEM, WB) sono disegnati come blocchi collegati, e attorno
ad essi sono distribuite le unità funzionali della FPU — l'Adder
(4 stadi), il Multiplier (7 stadi) e il Divider. Ogni blocco:

* si colora con il colore associato al proprio stadio quando contiene
  un'istruzione, e mostra il nome dell'istruzione all'interno;
* resta come un riquadro vuoto quando lo stadio è inattivo o contiene
  una bolla della pipeline (per esempio gli slot scartati da un salto
  oppure le bolle di drain a fine programma): come nel widget Swing,
  le bolle vengono mostrate come stadi vuoti;
* viene riempito con un tratteggio, il colore dedicato *Stall* e una
  breve etichetta del tipo di stallo quando in quel ciclo si è
  effettivamente verificato uno stallo. Le etichette riprendono la
  classificazione del widget Cycles del front-end Swing:

  - **RAW** — Read-After-Write (tipicamente sullo stadio ID quando
    il forwarding è disabilitato);
  - **WAW** — Write-After-Write fra due istruzioni FP che
    competono per lo stesso registro destinazione;
  - **Struct: Div / EX / FU** — stallo strutturale sul divisore
    FP, sullo stadio EX intero o su un'altra unità funzionale FP;
  - **Struct: Mem / Add / Mul** — stallo strutturale causato da
    un'istruzione bloccata in MEM, nell'ultimo stadio dell'Adder
    FP (A4) o nell'ultimo stadio del Multiplier FP (M7).

  Gli hazard WAR (Write-After-Read) *non* sono possibili in questa
  implementazione MIPS: l'emissione in-order in ID e il writeback
  tardivo in WB ordinano sempre le letture prima delle scritture
  successive, quindi il simulatore non li genera mai.

La classificazione degli stalli riusa il ``CycleBuilder`` del
front-end Swing — uno stallo viene identificato dal contatore della
CPU che è incrementato nell'ultimo ciclo — quindi il widget Pipeline
sul web è sempre coerente con i totali mostrati nel pannello
*Statistics*.

I colori dei singoli stadi (incluso quello *Stall*) possono essere
personalizzati dalla sezione *General Settings → Pipeline Colors*
(vedi sotto) e vengono salvati nel local storage del browser.

Registers
~~~~~~~~~
Contiene il valore dei registri general purpose interi, dei registri
in virgola mobile e del FCSR. I valori sono mostrati in
rappresentazione esadecimale; passando il mouse sopra un valore viene
mostrata l'interpretazione decimale corrispondente.

Memory
~~~~~~
Contenuto corrente della memoria principale simulata, organizzato in
celle indirizzabili. Ogni riga mostra l'indirizzo (in esadecimale) e
il valore della cella corrispondente; i tooltip rivelano il valore
decimale e le label/commenti sorgente associati alla cella.

Standard Output
~~~~~~~~~~~~~~~
Una text area in sola lettura che raccoglie tutto ciò che il
programma stampa tramite ``SYSCALL 4`` (scrittura intero) e
``SYSCALL 5`` (scrittura stringa). La ``SYSCALL 3`` (lettura stringa)
è supportata tramite una finestra popup: quando il programma in
esecuzione richiede un input, compare un *Input dialog* che chiede il
valore da fornire. Il dialogo rispetta la lunghezza massima
dichiarata dal programma e può essere annullato.

Cache Configuration
~~~~~~~~~~~~~~~~~~~
Permette di configurare i parametri del simulatore di cache L1:

* **Size** — capacità totale della cache in byte;
* **Block Size** — dimensione di una singola linea di cache, in byte;
* **Associativity** — numero di vie per insieme (``1`` per cache a
  mappatura diretta, ``>1`` per set-associative).

La cache L1 istruzioni e la cache L1 dati possono essere configurate
indipendentemente. I campi sono disabilitati durante l'esecuzione; la
nuova configurazione ha effetto al successivo reset.

General Settings
~~~~~~~~~~~~~~~~
Impostazioni persistenti che influenzano simulatore e UI. Tutti i
valori vengono salvati nel local storage del browser e sopravvivono
ai reload della pagina.

* **Editor Vi Mode** — abilita le combinazioni di tasti vi di base
  nell'editor.
* **Font Size** — dimensione del font per l'editor e per gli altri
  pannelli monospaziati; può essere modificata con i pulsanti ``-`` e
  ``+``.
* **Accordion Change Alerts** — abilita o disabilita il pallino
  pulsante mostrato sui pannelli chiusi quando il loro contenuto
  cambia.
* **CPU Forwarding** — abilita o disabilita il forwarding nella
  pipeline. Disabilitato durante l'esecuzione perché modificarlo
  richiede un reset della CPU.
* **Multi Step Size** — numero di cicli eseguiti da un singolo click
  del pulsante *Multi Step* sulla barra degli strumenti.
* **Execution Delay (ms)** — ritardo inserito tra blocchi successivi
  di cicli durante *Run All*. Aumentandolo, le esecuzioni lunghe
  diventano più lente in modo da poter seguire visivamente
  l'evidenziazione delle righe e l'aggiornamento dei pannelli. La
  modifica si applica in tempo reale, anche durante un'esecuzione in
  corso.
* **Pipeline Colors** — colori usati dal diagramma *Pipeline* per
  ciascuno stadio. Ogni voce (``IF``, ``ID``, ``EX``, ``MEM``, ``WB``,
  ``FP Adder``, ``FP Multiplier``, ``FP Divider``, ``Stall``) può
  essere modificata con un selettore di colore; il pulsante
  *Reset to defaults* ripristina la palette originale (gli stessi
  valori RGB usati dal front-end Swing).

Eseguire EduMIPS64 come applicazione desktop o da CLI
-----------------------------------------------------
Il frontend web è comodo perché non richiede installazione, ma
EduMIPS64 è distribuito principalmente come applicazione desktop
Java, che può anche essere eseguita da linea di comando. Il JAR
desktop espone funzionalità aggiuntive (una finestra di
configurazione più ricca, il Dinero frontend per l'analisi delle
trace di cache, opzioni CLI per esecuzione batch / headless, la
scrittura di tracefile) documentate nel manuale completo disponibile
su `Read the Docs <https://edumips64.readthedocs.io/>`_.

Per installare l'applicazione desktop o eseguire EduMIPS64 da linea
di comando, fai riferimento al repository GitHub del progetto:

* Pagina del progetto: https://www.edumips.org
* Codice sorgente, release e istruzioni d'installazione:
  https://github.com/EduMIPS64/edumips64

Se riscontri un bug o vuoi proporre un miglioramento del frontend
web, apri pure una issue su GitHub.
