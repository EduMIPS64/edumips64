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

EduMIPS64 è stato progettato e sviluppato da un gruppo di studenti
dell'Università degli Studi di Catania, ed ha tratto spunto, come interfaccia
e come funzionamento, dal simulatore WinMIPS64, sebbene vi siano alcune
differenze importanti con quest'ultimo.

Questo manuale vi introdurrà ad EduMIPS64, e spiegherà come utilizzarlo.

Il primo capitolo del manuale riguarda il formato dei file sorgente accettato
dal simulatore, descrivendo i tipi di dato e le direttive, oltre ai parametri da
linea di comando.

Nel secondo capitolo è presentata una panoramica del set di istruzioni
MIPS64 utilizzato da EduMIPS64, con tutti i parametri richiesti e le indicazioni per
il loro utilizzo.

Il terzo capitolo è una descrizione dell'interfaccia utente di EduMIPS64, che
espone lo scopo di ciascuna finestra e di ciascun menù, insieme ad una
descrizione delle finestre di configurazione, del Dinero frontend, del manuale e
delle opzioni da linea di comando. 

Il quarto capitolo contiene alcune esempi pratici di utilizzo del simulatore.

Questo manuale si riferisce ad EduMIPS64 versione 0.5.3.

.. toctree::
   :maxdepth: 2

   source-files-format
   instructions
