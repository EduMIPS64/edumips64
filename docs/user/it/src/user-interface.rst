L'interfaccia utente
====================
L'interfaccia grafica EduMIPS64 si ispira a quella di WinMIPS64. Infatti, la
finestra principale è identica, eccetto che per qualche menù.

.. Please refer to chapter~\ref{mips-simulators} for an overview of some MIPS
  and DLX simulators (including WinMIPS64), and to \cite{winmips-web} for more
  information about WinMIPS64. %In figure~\ref{fig:edumips-main} you can see
  the main window of EduMIPS64, composed by

La finestra principale di EduMIPS64 è caratterizzata da sei frame, che
mostrano i differenti aspetti della simulazione.  è inoltre presente una
barra di stato, che ha il duplice scopo di mostrare il contenuto delle celle
di memoria e dei registri quando vengono selezionati e di notificare
all'utente che il simulatore è in esecuzione quando la simulazione è
stata avviata ma la modalità verbose non è stata attivata.

La barra di stato mostra inoltre lo stato in cui si trova la CPU. Può mostrare
uno dei quattro seguenti stati:

* *READY* La CPU non ha caricato alcuna istruzione (nessun programma è stato
  caricato).
* *RUNNING* La CPU sta eseguendo una serie di istruzioni.
* *STOPPING* La CPU ha riscontrato un'istruzione di terminazione,
  e sta eseguendo le istruzioni già presenti nella pipeline prima di
  terminare il programma.
* *HALTED* La CPU ha terminato l'esecuzione del programma.

Nota che lo stato della CPU è differente dallo stato del simulatore. Il
simulatore può eseguire un numero finito di cicli di CPU e fermarsi,
consentendo all'utente di ispezionare memoria e registri: in questo stato
intermedio tra cicli di CPU, la CPU rimane comunque in stato *RUNNING* o
*STOPPING*. Una volta che la CPU raggiunge lo stato *HALTED*, l'utente non
può più eseguire alcun ciclo di CPU senza caricare nuovamente un programma
(lo stesso, od uno differente).

Maggiori dettagli sono descritti nelle sezioni a seguire.

La barra dei menù
-----------------
La barra del menù contiene sei opzioni:

File
~~~~
Il menù File contiene comandi per l'apertura dei file, per resettare o
fermare il simulatore e per scrivere i trace file.

* *Apri...* Apre una finestra che consente all'utente di scegliere un file
  sorgente da aprire.

* *Apri recente* Mostra la lista dei file recentemente aperti dal simulatore,
  dalla quale l'utente può scegliere il file da aprire.

* *Resetta* Inizializza nuovamente il simulatore, mantenendo aperto il file
  che era stato caricato ma facendone ripartire l'esecuzione.

* *Scrivi Tracefile Dinero...* Scrive i dati di accesso alla memoria in un
  file, nel formato xdin.

* *Esci* Chiude il simulatore.

La voce del menù *Scrivi Tracefile Dinero...* è disponibile solo quando un
file sorgente è stato completamente eseguito ed è stata già raggiunta la fine
dell'esecuzione.

Esegui
~~~~~~
Il menu Esegui contiene voci riguardanti il flusso di esecuzione della
simulazione.

* *Ciclo singolo* Esegue un singolo passo di simulazione.

* *Completa* Inizia l'esecuzione, fermandosi quando il simulatore
  raggiunge una SYSCALL 0 (o equivalente) o un'istruzione di `BREAK`,
  oppure quando l'utente seleziona la voce Stop del menù (o preme F9).

* *Cicli multipli* Esegue un certo numero di passi di simulazione, tale
  valore può essere configurato attraverso la finestra di configurazione.

.. Vedere la sezione~\ref{dialog-settings} per ulteriori dettagli.

* *Ferma* Ferma l'esecuzione quando il simulatore è in modalità
  "Completa" o "Cicli multipli", come descritto precedentemente.

Il menu è disponibile solo quando è stato caricato un file sorgente e
non è ancora stato raggiunto il termine della simulazione.  La voce
*Stop* del menù  disponibile solo in modalità "Completa" o
"Cicli multipli" mode.

Configura
~~~~~~~~~
Il menu Configura fornisce l'opportunità di personalizzare l'aspetto ed il
funzionamento di EduMIPS64.

* *Impostazioni...* Apre la finestra di configurazione, descritta nella
  prossima sezione di questo capitolo;

* *Selezione lingua* Consente di modificare la lingua usata
  dall'interfaccia utente. Attualmente sono supportate solo inglese ed
  italiano. Questa modifica riguarda ogni aspetto dell'interfaccia grafica,
  dal titolo delle finestre al manuale in linea ed i messaggi di errore o le
  notifiche.

La voce di menù `Impostazioni...` non è disponibile quando il
simulatore è in modalità "Completa" o "Cicli multipli", a causa di
possibili race conditions.

Il simulatore rallenta notevolmente quando l'interfaccia grafica va aggiornata
in tempo reale. Per eseguire grandi programmi (migliaia di cicli) più
velocemente, disabilitare l'opzione "Sincronizza la GUI con la CPU
nell'esecuzione multi step".

Strumenti
~~~~~~~~~
Questo menù contiene solo una voce, utilizzata per aprire la finestra del
Dinero frontend.

* *Dinero Frontend...* Apre la finestra del Dinero Frontend..

Questo menù non è disponibile finchè non è stata portata a
termine l'esecuzione del programma

Finestra
~~~~~~~~
Questo menù contiene voci relative alle operazioni con le finestre.

* *Tile* Ordina le finestre visibili in modo tale che non vi siano
  più di tre finestre in una riga, tentando di massimizzare lo spazio
  occupato da ciascuna finestra.

Le altre voci del menù modificano semplicemente lo stato di ciascuna
finestra, rendendola visibile o riducendola ad icona.

Aiuto
~~~~~
Questo menù contiene voci relative all'aiuto in linea.

* *Manuale...* Mostra la finestra di help.
* *Informazioni su...* Mostra una finestra contenente i nomi di coloro
  che hanno collaborato al progetto ed i loro ruoli.

Finestre
--------
L'interfaccia grafica è composta da sette finestre, sei delle quali sono
visibili per default, mentre una (la finestra di I/O) è nascosta.

Cicli
~~~~~
La finestra Cicli mostra l'evoluzione del flusso di esecuzione nel tempo,
visualizzando in ogni istante quali istruzioni sono nella pipeline, ed in
quale stadio si trovano.

Registri
~~~~~~~~
La finestra Registri mostra il contenuto di ciascun registro. Mediante un
click col tasto sinistro del mouse è possibile vedere il loro valore
decimale (con segno) nella barra di stato, mentre con un doppio click
verrà aperta una finestra di dialogo che consentirà all'utente di
cambiare il valore del registro

Statistics
~~~~~~~~~~
La finestra Statistiche mostra alcune statistiche riguardanti l'esecuzione del
programma.

Nota che durante l'ultimo ciclo di esecuzione il contatore dei cicli non viene
incrementato, perché l'ultimo ciclo non è un vero ciclo di CPU ma solo uno
pseudo-ciclo che ha l'unico compito di rimuovere l'ultima istruzione dalla
pipeline ed incrementare il contatore delle istruzioni eseguite.

Pipeline
~~~~~~~~
La finestra Pipeline mostra lo stato attuale della pipeline, visualizzando
ciascuna istruzione con il suo stadio.  I differenti colori evidenziano i vari
stadi della pipeline stessa.

Memoria
~~~~~~~
La finestra Memoria mostra il contenuto delle celle di memoria, insieme alle
etichette ed i commenti, tratti dal codice sorgente. Il contenuto delle celle
di memoria, come per i registri, può essere modificato con un doppio
click, e mediante un singolo click del mouse verrà mostrato il loro valore
decimale nella barra di stato.  La prima colonna mostra l'indirizzo
esadecimale della cella di memoria, e la seconda il valore della cella stessa.
Le altre colonne mostrano invece informazioni addizionali provenienti dal
codice sorgente.

Codice
~~~~~~
La finestra Codice visualizza le istruzioni caricate in memoria.. La prima
colonna mostra l'indirizzo dell'istruzione, mentre la seconda mostra la
rappresentazione esadecimale dell'istruzione stessa. Le altre colonne mostrano
infine informazioni addizionali provenienti dal codice sorgente.

Input/Output
~~~~~~~~~~~~
La finestra Input/Output fornisce un'interfaccia all'utente per la
visualizzazione dell'output creato dai programmi mediante le SYSCALL 4 e 5.
Attualmente non è utilizzata per l'input di dati, ed al suo posto viene
utilizzata una finestra di dialogo che viene mostrata quando una SYSCALL 3
tenta di leggere dallo standard input, ma future versioni includeranno una
casella di testo per l'input.

Finestre di dialogo
-------------------
Le finestre di dialogo sono utilizzate da EduMIPS64 per interagire con l'utente
in vari modi. Ecco un riassunto delle più importanti:

Impostazioni
~~~~~~~~~~~~
Nella finestra di configurazione possono essere configurati vari aspetti del
simulatore. La selezione del tasto "OK" causa il salvataggio delle modifiche
apportate alla configurazione, mentre il tasto "Cancel" (o la semplice
chiusura della finestra) comporta il mancato salvataggiu delle stesse.

La sezione "Impostazioni generali" consente di configurare il forwarding ed il
numero di passi da effettuare nella modalità Cicli multipli.

La sezione "Comportamento" permette di abilitare o disabilitare gli avvisi
durante la fase di parsing, l'opzione "sincronizza la GUI con la CPU
nell'esecuzione multi step", quando abilitata, sincronizzerà lo stato
grafico delle finestre con lo stato interno del simulatore. Ciò
implicherà una simulazione più lenta, ma con la possibilità di
avere un resoconto grafico esplicito di ciò che sta avvenendo durante la
simulazione.  L'opzione "intervallo tra i cicli", qualora sia abilitata,
influenzerà il numero di millisecondi che il simulatore dovrà
attendere prima di cominciare un nuovo ciclo. Tali opzioni hanno effetto solo
quando la simulazione è avviata utilizzando le opzioni "Completa" o "Cicli
multipli" dal menu Esegui.

Le ultime due opzioni stabiliscono il comportamento del simulatore quando si
verifica un'eccezione sincrona.  è importante notare che se le eccezioni
sincrone sono mascherate, non succederà nulla, anche se l'opzione "Termina
se si verifica un'eccezione sincrona" è abilitata. Se le eccezioni non
sono mascherate e tale opzione è abilitata, apparirà una finestra di
dialogo, e la simulazione sarà fermata non appena tale finestra verrà
chiusa.

L'ultima sezione permette di modificare l'aspetto dell'interfaccia utente. Ci
sono opzioni per cambiare i colori associati ai diversi stadi della pipeline,
un'opzione per scegliere se mostrare i valori delle celle di memoria come long
o come double ed un'opzione per impostare la dimensione del font
dell'interfaccia.

Nota: lo scaling proporzionale dell'interfaccia rispetto alla dimensione del
font non funziona benissimo, ma dovrebbe essere sufficiente a rendere il
simulatore utilizzabile su schermi ad alta risoluzione (e.g., 4k).

Dinero Frontend
~~~~~~~~~~~~~~~
La finestra di dialogo Dinero Frontend consente di avviare un processo
DineroIV con il trace file generato internamente mediante l'esecuzione del
programma. Nella prima casella di testo c'è il percorso dell'eseguibile
DineroIV, e nella seconda devono essere inseriti i parametri opportuni.

.. % Please see~\cite{dinero-web} for further informations about the DineroIV
   cache simulator.

La sezione più in basso contiene l'output del processo DineroIV, dal quale
è possibile prelevare i dati di cui si necessita.

Aiuto
~~~~~
La finestra di Aiuto contiene il manuale del simulatore, che è una copia HTML
del presente documento.

Opzioni da riga di comando
--------------------------
Sono disponibili quattro opzioni da linea di comando. Esse sono descritte di
seguito, con il nome per esteso scritto tra parentesi.  Nomi abbreviati e per
esteso possono essere utilizzati indifferentemente.

* `-v (--version)` stampa la versione del simulatore ed esce.

* `-h (--help)` mostra un messaggio di aiuto per le opzioni da linea di
  comando ed esce.

* `-f (--file) filename` apre `filename` nel simulatore.

* `-r (--reset)` ripristina i valori predefiniti per tutti i parametri di
  configurazione

* `-d (--debug)` attiva la modalità di debugging.

* `-hl (--headless)` Esegue EduMIPS64 in modalità headless (senza interfaccia grafica)

Nella modalità di debugging è disponibile una nuova finestra, la finestra
Debug, che mostra il resoconto delle attività interne di  EduMIPS64. Tale
finestra non è utile per l'utente finale, è stata infatti ideata per
poter essere utilizzata dagli sviluppatori di EduMIPS64.

Eseguire EduMIPS64
------------------
Il file `.jar` di EduMIPS64 può funzionare sia come file .jar
eseguibile che come applet, quindi può essere eseguito in entrambi i modi,
che richiedono il Java Runtime Environment, versione 5 o successiva.

Per eseguire il file come applicazione a sè stante, l'eseguibile
`java` deve essere avviato nel seguente modo: `java -jar
edumips64-version.jar`, dove la stringa `version` deve essere
sostituita con la versione attuale del simulatore.  Su alcuni sistemi,
potrebbe essere possibile eseguire il programma semplicemente con un click sul
file `.jar`.

Per eseguire il file come applet deve essere utilizzato il tag
`<applet>`. Il sito web di EduMIPS64 presenta una pagina già contenente
l'applet, in modo tale che chiunque possa eseguire il programma senza il
problema dell'utilizzo da linea di comando.
