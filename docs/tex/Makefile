IT_FILENAME=manual_it
EN_FILENAME=manual

all-dvi:
	latex ${IT_FILENAME}.tex
	latex ${IT_FILENAME}.tex
	latex ${IT_FILENAME}.tex
	latex ${EN_FILENAME}.tex
	latex ${EN_FILENAME}.tex
	latex ${EN_FILENAME}.tex

it-dvi:
	latex ${IT_FILENAME}.tex
	latex ${IT_FILENAME}.tex
	latex ${IT_FILENAME}.tex

en-dvi:
	latex ${EN_FILENAME}.tex
	latex ${EN_FILENAME}.tex
	latex ${EN_FILENAME}.tex

it-pdf: it-dvi
	dvipdf ${IT_FILENAME}.dvi

en-pdf: en-dvi
	dvipdf ${EN_FILENAME}.dvi

all-pdf: it-dvi en-dvi
	dvipdf ${IT_FILENAME}.dvi
	dvipdf ${EN_FILENAME}.dvi

clean:
	rm *.log *.aux *.bbl *.blg *.toc *.idx *.ilg *.ind *~ *.dvi *.pdf *.lof *.lol *.lot
