Capire le versioni
==================
Ogni build di EduMIPS64 mostra un identificatore di versione, così sai sempre
esattamente cosa stai eseguendo. Questa pagina spiega il significato
dell'identificatore e dove trovarlo.

Il numero di versione
---------------------
In corrispondenza di una release ufficiale, la versione è un numero semplice,
per esempio ``1.4.1``.

Una build realizzata *tra* due release ha un aspetto simile a
``1.4.0-74-geec1768``. Leggendolo da sinistra a destra:

* ``1.4.0`` — la release ufficiale più recente a cui questa build fa seguito.
* ``74`` — il numero di modifiche apportate dopo quella release.
* ``geec1768`` — un breve identificatore univoco per questa build specifica.

Non è necessario memorizzare l'identificatore. Il suo scopo principale è
consentirti di indicare la *build esatta* che stai utilizzando quando segnali
un bug, in modo che i mantainer possano riprodurre il problema senza dover
fare supposizioni.

Dove trovare la versione
------------------------
* **Applicazione desktop (Swing)** — mostrata nel titolo della finestra e
  nella finestra di dialogo *Help → About*.
* **Riga di comando** — esegui il JAR con il flag ``--version``.
* **Applicazione web** — mostrata nella scheda **About** della finestra di
  aiuto e accanto all'etichetta **"Web Version"** nella barra degli strumenti
  in alto.

Quale build web sto eseguendo?
-------------------------------
L'applicazione web di **produzione** all'indirizzo https://web.edumips.org
contiene la versione stabile. Viene aggiornata solo quando i mantainer
*promuovono* esplicitamente una nuova build, quindi è la scelta più sicura per
un utilizzo quotidiano.

Anche ogni commit del codice di sviluppo viene compilato e pubblicato a un
proprio indirizzo permanente sotto ``https://web.edumips.org/c/<commit>/``. Un
piccolo badge colorato accanto all'etichetta **"Web Version"** nella barra
degli strumenti indica su quale tipo di build ti trovi:

* **Nessun badge** — ti trovi sulla normale build di produzione. È la versione
  stabile di https://web.edumips.org.
* **``ARCHIVED``** — stai visualizzando una build specifica di un singolo
  commit, servita da ``/c/<commit>/``. Può trattarsi di una versione promossa
  in passato o di un candidato in attesa di promozione; in ogni caso non è il
  sito di produzione attivo. Passando con il mouse sul badge viene mostrato un
  tooltip che lo conferma.
* **``PR #N``** (badge giallo) — un'anteprima temporanea creata per una
  specifica modifica proposta (pull request) su GitHub. Cliccando sul badge si
  apre la pagina della pull request corrispondente.
* **``dev``** (badge blu) — qualcuno sta eseguendo una build di sviluppo
  locale direttamente sul proprio computer.

In sintesi: **nessun badge → produzione stabile; ``ARCHIVED`` → una build
specifica di un singolo commit; ``PR #N`` / ``dev`` → build temporanee o di
anteprima.**

Sfogliare e cambiare versione
-----------------------------
Tutte le build web conservate — sia le release promosse sia i candidati in
attesa — sono elencate in un unico posto, identificate dal loro commit. Apri
l'elenco dalla finestra di dialogo **Help**:

1. Fai clic su **Help** → **About** (nella barra degli strumenti dell'app web).
2. La scheda About mostra due elenchi:

   * **Promoted versions** — le release promosse manualmente, mostrate in
     evidenza. Quella attiva è contrassegnata come **current**.
   * **Candidate builds** — le build più recenti non ancora promosse. Vengono
     mostrati tutti i candidati in attesa (di solito sono pochi).
3. Fai clic su una voce qualsiasi per aprire quella build esatta in una nuova
   scheda — il tuo lavoro attuale viene preservato nella scheda originale.

Cosa viene conservato, e per quanto tempo
-----------------------------------------
* Le **versioni promosse** vengono conservate in modo permanente come snapshot
  immutabili, così puoi sempre tornare a qualunque release.
* Le **build candidate** vengono conservate fino a quando non avviene una
  promozione. Quando una build viene promossa, i candidati che la *precedevano*
  (e non sono mai stati promossi) vengono rimossi; i candidati più recenti
  della build promossa vengono preservati e restano disponibili finché non
  vengono a loro volta promossi o superati.

In questo modo l'elenco resta essenziale: ogni release promossa più i pochi
candidati ancora in attesa di promozione.

