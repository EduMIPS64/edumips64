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
promuovono esplicitamente una nuova release, quindi è la scelta più sicura per
un utilizzo quotidiano.

Esistono anche altre build web, identificate da un piccolo badge colorato
mostrato accanto all'etichetta **"Web Version"** nella barra degli strumenti:

* **Nessun badge** — ti trovi sulla normale build di produzione. È la versione
  stabile di https://web.edumips.org.
* **``NIGHTLY``** (badge arancione) — una build che viene **ricostruita
  automaticamente ogni notte** dall'ultimo codice in sviluppo. Contiene sempre
  le funzionalità più recenti, ma **potrebbe essere instabile**. Passando con
  il mouse sul badge viene mostrato un tooltip che lo conferma.
* **``PR #N``** (badge giallo) — un'anteprima temporanea creata per una
  specifica modifica proposta (pull request) su GitHub. Cliccando sul badge si
  apre la pagina della pull request corrispondente.
* **``dev``** (badge blu) — qualcuno sta eseguendo una build di sviluppo
  locale direttamente sul proprio computer.

In sintesi: **nessun badge → produzione stabile; ``NIGHTLY`` → funzionalità
più recenti ma possibilmente instabile; ``PR #N`` / ``dev`` → build
temporanee o di anteprima.**
