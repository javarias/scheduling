SRC=SchedulingAPRCTool.xml

all: html pdf 

html:
	docbook2html $(SRC) -u

pdf:
	htmldoc -f SchedulingAPRCTool.pdf SchedulingAPRCTool.html
clean:
	rm -rf *.html *.pdf
