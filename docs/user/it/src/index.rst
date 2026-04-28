.. EduMIPS64 documentation master file, created by
   sphinx-quickstart on Tue Apr 26 23:10:10 2011.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Benvenuti nella documentazione di EduMIPS64!
============================================
EduMIPS64 è un simulatore di Instruction Set Architecture (ISA) MIPS64,
progettato per eseguire piccoli programmi che utilizzino il sottoinsieme
dell'Instruction Set MIPS64 implementato dal simulatore stesso; permette
all'utente di vedere come le istruzioni si comportino nella pipeline, come gli
stalli siano gestiti dalla CPU, lo stato di registri e memoria e molto altro.
È classificabile sia come simulatore, sia come debugger visuale.

Il sito web del progetto è http://www.edumips.org, ed il codice sorgente è
disponibile presso http://github.com/EduMIPS64/edumips64. Per segnalare bug o
inviare suggerimenti sul simulatore, è possibile aprire una issue su github o
inviare una mail a bugs@edumips.org.

EduMIPS64 è stato progettato e sviluppato da un gruppo di studenti
dell'Università degli Studi di Catania, ed ha tratto spunto, come interfaccia
e come funzionamento, dal simulatore WinMIPS64, sebbene vi siano alcune
differenze importanti con quest'ultimo.

Questo manuale vi introdurrà ad EduMIPS64, e spiegherà come utilizzarlo.

Questo manuale si riferisce ad EduMIPS64 versione |version|.

Il manuale è suddiviso in due parti. La prima parte è indipendente
dall'interfaccia utente in uso e copre il formato dei file sorgente,
l'insieme di istruzioni supportato, la Floating Point Unit e una serie
di programmi di esempio. La seconda parte documenta le interfacce
utente: un capitolo dedicato all'applicazione desktop (Swing), che
include anche le opzioni da linea di comando del JAR, un capitolo
dedicato alla shell interattiva a linea di comando esposta dal JAR in
modalità headless, ed un capitolo dedicato al frontend web.

Quando il manuale viene aperto dall'interno dell'applicazione, viene
mostrato solo il capitolo relativo all'interfaccia utente attiva. Il
manuale completo (con entrambi i capitoli sulle interfacce utente) è
disponibile su `Read the Docs <https://edumips64.readthedocs.io/>`_ ed
in formato PDF.

.. toctree::
   :maxdepth: 2

   source-files-format
   instructions
   fpu
   examples

.. only:: not web

   .. toctree::
      :maxdepth: 2

      user-interface-swing
      cli-interface

.. only:: not swing

   .. toctree::
      :maxdepth: 2

      user-interface-web
