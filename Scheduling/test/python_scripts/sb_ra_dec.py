import ppgplot
from ppgplot import *
from numarray import array
import sys

if len(sys.argv) < 2:
    print "\nScript Usage: "
    print "\python sb_ra_dec.ps <stats filename> "
    print ""
    sys.exit()

#colors for different frequency bands
band1 = 2
band2 = 3
band3 = 4
band4 = 5
band5 = 6
band6 = 7
band7 = 8
band8 = 9
band9 = 10
band10 = 11

#for each SB.
sbs = {}
totalHighest = 0.0
file1 = open(sys.argv[1],"r")
#file1 = open("stats_output.txt","r")
for line in file1.readlines():
   if not line.startswith("##"):
        l = line.split()
        if len(l) > 0:        
            key = l[0].strip(',')
            if not sbs.has_key(key):
                sbs[key] = []
            sbs[key].append(l)

l = len(sbs)/8
pgbeg("ra_dec.ps/cps",8, int(l))
pgpap(8.0,1.0)
pgsvp(0.1, 0.9, 0.1, 0.9)
pgpage()
pnum =1
for iter in sbs.iterkeys():
    if pnum > 8:
        pnum = 1
    ra =[]
    dec = []
    band=[]
    for x in sbs[iter]:
        band.append(int(x[6].strip(',')))
        ra.append(float(x[7].strip(',')))
        dec.append(float(x[8].strip(',')))
    
    highest = [max(ra), max(dec)]
    pgswin(0.0, max(highest)+0.5, 0.0, max(highest)+0.5)
    pgbox('BSCNT1',0,0,'BSCNTV',0,0)
    pgsci(1)
    if max(highest) > totalHighest:
        totalHighest = max(highest)
    i=0
    while i <  len(ra):
        if band[i] == 1:
            pgsci(band1)
    
        if band[i] == 2:
            pgsci(band2)
    
        if band[i] == 3:
            pgsci(band3)
    
        if band[i] == 4:
            pgsci(band4)
    
        if band[i] == 5:
            pgsci(band5)
    
        if band[i] == 6:
            pgsci(band6)
    
        if band[i] == 7:
            pgsci(band7)
    
        if band[i] == 8:
            pgsci(band8)
    
        if band[i] == 9:
            pgsci(band9)

        if band[i] == 10:
            pgsci(band10)

        i +=1
    arrayRA = array(ra)
    arrayDEC = array(dec)
    pgpt(arrayRA, arrayDEC, 2281)
    
    pgsci(1)
    pglab("RA","DEC","SB "+str(iter)+" on band "+str(band[0]))
    pgpage()
    pnum +=1


#one plot including all SBs, will be a big ol' cluster if RA/DEC not diverse
pgsci(1)
pgbeg("allRaDec.ps/cps",1,1)
pgpap(8.0,1.0)
pgsvp(0.1, 0.9, 0.1, 0.9)
pgswin(0.0, totalHighest+0.5, 0.0, totalHighest+0.5)
pgbox('BSCNT1',0,0,'BSCNTV',0,0)
pglab("RA","DEC","All SBs")
allRa = []
allDec = []
i=0
for iter in sbs.iterkeys():
    for x in sbs[iter]:
        band = int(x[6].strip(','))
        if band == 1:
            pgsci(band1)
    
        if band == 2:
            pgsci(band2)
    
        if band == 3:
            pgsci(band3)
    
        if band == 4:
            pgsci(band4)
    
        if band == 5:
            pgsci(band5)
    
        if band == 6:
            pgsci(band6)
    
        if band == 7:
            pgsci(band7)
    
        if band == 8:
            pgsci(band8)
    
        if band == 9:
            pgsci(band9)

        if band == 10:
            pgsci(band10)

        pgcirc(float(x[7].strip(',')), float(x[8].strip(',')),0.02)

pgsci(1)
pgsch(0.75)
pgmtxt('T',1, 0.5, 0.5, "NOTE: Some RA/Dec points might be overlapping")

pgend()
