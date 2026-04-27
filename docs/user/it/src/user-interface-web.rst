L'interfaccia utente web
========================
EduMIPS64 è disponibile anche come applicazione web che gira interamente
nel browser. Il core del simulatore è cross-compilato da Java a
JavaScript ed è eseguito come Web Worker, mentre l'interfaccia utente è
realizzata con React. Il deployment di produzione è ospitato su
https://web.edumips.org.

Questo capitolo descrive il frontend web. Per il formato dei file
sorgente, il set di istruzioni, la FPU e gli esempi di programmi, fai
riferimento agli altri capitoli del manuale: quei contenuti sono
indipendenti dall'interfaccia utente.

Avvio rapido
------------
Dopo aver caricato o scritto un programma nell'editor di codice, usa la
barra degli strumenti per eseguire la simulazione passo passo, avviarla
o resettarla. Passando il mouse su un'istruzione vengono mostrate
informazioni su di essa: l'indirizzo, la rappresentazione binaria,
l'opcode e, se l'istruzione è attualmente in pipeline, lo stadio della
CPU in cui si trova nel ciclo corrente. Gli stadi della CPU sono inoltre
codificati con colori diversi.

Layout
------
Il frontend web è organizzato in pannelli che mostrano i diversi aspetti
della simulazione in esecuzione:

* **Codice** — il programma caricato in memoria, con l'indirizzo e la
  rappresentazione esadecimale di ciascuna istruzione.
* **Registri** — il contenuto dei registri interi e in virgola mobile.
* **Memoria** — il contenuto delle celle di memoria.
* **Pipeline** — le istruzioni attualmente presenti in ciascun stadio
  della pipeline della CPU. Gli stadi sono codificati con colori.
* **Statistiche** — contatori relativi all'esecuzione del programma
  (cicli eseguiti, istruzioni eseguite, stalli, ecc.).

Caricare ed eseguire programmi
------------------------------
I programmi possono essere scritti direttamente nell'editor di codice
oppure caricati da uno degli esempi inclusi. Il simulatore supporta lo
stesso formato di file sorgente descritto in
:doc:`source-files-format`.

Una volta caricato un programma è possibile:

* Eseguire un singolo ciclo di CPU (single step).
* Eseguire il programma fino al termine, ovvero fino ad una
  ``SYSCALL 0`` (o equivalente) o ad una istruzione ``BREAK``.
* Resettare il simulatore allo stato iniziale, mantenendo il programma
  caricato.

Aiuto e lingua
--------------
La finestra di aiuto (icona con il punto interrogativo nella barra
superiore) apre questo manuale all'interno dell'applicazione, con un
pannello di navigazione sulla sinistra e un selettore della lingua che
consente di scegliere tra italiano, inglese e cinese.

La scheda "About" della finestra di aiuto mostra la versione del
simulatore e una descrizione della build in esecuzione (produzione,
anteprima per pull request o sviluppo locale).

Eseguire EduMIPS64 come applicazione desktop o da CLI
-----------------------------------------------------
Il frontend web è comodo perché non richiede installazione, ma
EduMIPS64 è distribuito principalmente come applicazione desktop Java,
che può anche essere eseguita da linea di comando. Il JAR desktop
espone funzionalità aggiuntive (finestra di configurazione completa,
simulatore di cache L1, Dinero frontend, opzioni CLI per l'esecuzione
batch / headless) documentate nel manuale completo disponibile su
`Read the Docs <https://edumips64.readthedocs.io/>`_.

Per installare l'applicazione desktop o eseguire EduMIPS64 da linea di
comando, fai riferimento al repository GitHub del progetto:

* Pagina del progetto: https://www.edumips.org
* Codice sorgente, release e istruzioni d'installazione:
  https://github.com/EduMIPS64/edumips64

Se riscontri un bug o vuoi proporre un miglioramento del frontend web,
apri pure una issue su GitHub.
