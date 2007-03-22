#!/usr/bin/env python

import ppgplot
from ppgplot import *
from numarray import array
from math import *
import sys

if len(sys.argv) <2:
    print "\nScript Usage: "
    print "\tALMASchedSim_antennaLocation.py <input filename>"
    print ""
    sys.exit()



antennaX = []
antennaY = []
antennaZ = []
config=""
datum =""
zone = ""
antnum = ""
file = open(sys.argv[1],"r")
for l in file.readlines():
    if l.startswith("Antenna."):
        line = l.split()
        antennaX.append(float(line[3].strip(';')))
        antennaY.append(float(line[4].strip(';')))
        antennaZ.append(float(line[5].strip(';')))
    if l.startswith("Site.antennaConfiguration"):
        s = l.split();
        config = s[2]
    if l.startswith("Site.datum"):
        s = l.split()
        datum = s[2:]
    if l.startswith("Site.zone"):
        s = l.split()
        zone = s[2:]
    if l.startswith("Site.numberAntennas"):
        s = l.split()
        antnum = str(s[2])

centerX = 627790.23
centerX0 = 0
centerY = 7453070.95
centerY0 = 0
#get viewport coordinates so that the center is really the center
x1=0
x2=0
y1 =0
y2 = 0
if min(antennaX) < centerX:
    x1 = centerX - min(antennaX)
else:
    x1 = min(antennaX) - centerX

if max(antennaX) < centerX:
    x2 = centerX - max(antennaX)
else:
    x2 = max(antennaX) - centerX

if min(antennaY) < centerY:
    y1 = centerY - min(antennaY)
else:
    y1 = min(antennaY) - centerY

if max(antennaY) < centerY:
    y2 = centerY - max(antennaY)
else:
    y2 = max(antennaY) - centerY

foo = [x1,x2,y1,y2]
bound = ceil(max(foo))

antennaX0 =[]
antennaY0 =[]
for x in antennaX:
    antennaX0.append(x - centerX)

for x in antennaY:
    antennaY0.append(x - centerY)


#pgbeg("/xw", 1, 1) 
#pgbeg("antenna_positions.ps/cps", 1, 1) #to file
pgbeg("antenna_positions.gif/GIF", 1, 1) #to file
pgpap(8.0,1.0)
pgsvp(0.1, 0.9, 0.1, 0.9 )

color=1
pgsci(color) # color
pgsfs(2) #fill style = outline

pgswin(centerX0-bound, centerX0+bound, centerY0-bound, centerY0+bound)
pgbox('BSCNT1',0,0,'BSCNTV1',0,0)
pglab("X position (m)", "Y position (m)","Configuration: "+config+" ("+antnum+")")
#pgmtxt('T', 1, 0.5,0.5,"Configuration: "+ config)
pgsch(0.5)
pgmtxt('B', 2, 1.0, 1.0,"Center X = "+str(centerX))
pgmtxt('B', 3, 1.0, 1.0,"Center Y = "+str(centerY))
z=""
for x in zone:
    z=z+ " "+str(x)

pgmtxt('B', 4, 1.0, 1.0,"Zone: "+ z)
d = ""
for x in datum:
    d = d+ " "+str(x)

pgmtxt('B', 5, 1.0, 1.0,"Datum: "+d)
pgsch(1.0)

xarray = array(antennaX0)
yarray = array(antennaY0)
pgpt(xarray,yarray, 530)

pgcirc(centerX0, centerY0, 500) #1KM diameter
pgcirc(centerX0, centerY0, 250) #1/2KM diameter
pgcirc(centerX0, centerY0, 125) #1/4KM diameter
pgcirc(centerX0, centerY0, 62) #1/8KM diameter
pgcirc(centerX0, centerY0, 31) #1/16KM diameter

#pgiden()

centerColor=2
pgsci(centerColor)
pgsfs(1)
pgcirc(centerX0, centerY0, 1)
pgsci(color)
pgsfs(2)

pgend()
