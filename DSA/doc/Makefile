SRC=algorithm

all:
	pdflatex $(SRC).tex
	pdflatex $(SRC).tex

clean:
	-rm -f $(SRC).{aux,toc,log,tmp,dvi,idx,ilg,ind,out,bbl,blg,lol,lof,lot} .my_log include/*.aux

distclean: clean
	-rm -f $(SRC).pdf

