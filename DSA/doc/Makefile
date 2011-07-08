SRC=algorithm

all: tsyscrit.mps
	pdflatex $(SRC).tex
	pdflatex $(SRC).tex

clean:
	-rm -f $(SRC).{aux,toc,log,tmp,dvi,idx,ilg,ind,out,bbl,blg,lol,lof,lot} .my_log include/*.aux
	-rm -f *.mps *.0

distclean: clean
	-rm -f $(SRC).pdf

tsyscrit.mps: tsyscrit.mp
	mpost tsyscrit.mp
	mv tsyscrit.0 tsyscrit.mps


