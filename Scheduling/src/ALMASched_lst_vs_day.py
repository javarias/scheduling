#!/usr/bin/env python
import ppgplot
from ppgplot import *
import sys
from math import floor
from math import ceil
from numarray import array
import time
import string

#print sys.argv[0] #script name
#print sys.argv[1] # stats filename
#print sys.argv[2] #graph name
#print sys.argv[3] #input filename
#print len(sys.argv)
if len(sys.argv) < 4:
    print "\nScript Usage: "
    print "\tALMASched_lst_vs_day.py <stats filename> <graph output filename> <inputfile>"
    print ""
    sys.exit()

#colors for different frequency bands

#for each SB.
lststart= []
startday = []
endday = []
execlen=[]
sbname=[]
midnights =[]
firstdaymnlst=0.0
lastdaymnlst=0.0
weathernames=[]
freq_bands=[]
file1 = open(sys.argv[1],"r")
#file1 = open("stats_output.txt","r")
for line in file1.readlines():
   if not line.startswith("##"):
        l = line.split()
        if len(l) > 0:
            sbname.append(l[0].strip(','))
            lststart.append(float(l[2].strip(',')))
            execlen.append(float(l[4].strip(',')))
            startday.append(float(l[14].strip(',')))
            endday.append(float(l[15].strip(',')))
            midnights.append(float(l[16].strip(',')))
            firstdaymnlst =(float(l[17].strip(',')))
            lastdaymnlst =(float(l[18].strip(',')))
            weathernames.append(l[10].strip(','))
            freq_bands.append(int(l[6].strip(',')))

#Automatically display results
#pgbeg("/xw",1,1)
#Save to color post script file            
#pgbeg(sys.argv[2]+".ps/cps",1,1) 
#Save to gif file            
foo = "_tmp_schedule.gif"   
pgbeg(foo+"/GIF",1,1)
pgpap(10.5,0.75)
color =1
pgsci(color)
#left side labelling
#pgswin(0, 24, 31+0.5, 0-0.5);
pgswin(0, 24, max(endday)+1.5, min(startday)-1.5)
pgbox('',0,0,'NV',1,0)
#right side labelling
#pgswin(0, 24, 32+.5,1-0.5)
pgswin(0, 24, max(endday)+2.5, min(startday)-0.5)
pgbox('',0,0,'MV',1,0)
pglab("LST", "Calander Day", "Schedule")
pgswin(0, 24, max(endday)+2.0, min(startday)-1.0)
pgbox('BSCNT1',0,0,'BSCTV',1,0)

i =0

band1= 3
band2= 5
band3= 6
band4=12 
band5= 7
band6= 8
band7= 2
band8= 4
band9= 11
band10=10 

name =""
while i < len(lststart):
    if str(freq_bands[i]) == "1":
        pgsci(band1)
    
    if str(freq_bands[i]) == "2":
        pgsci(band2)
    
    if str(freq_bands[i]) == "3":
        pgsci(band3)
    
    if str(freq_bands[i]) == "4":
        pgsci(band4)
    
    if str(freq_bands[i]) == "5":
        pgsci(band5)
    
    if str(freq_bands[i]) == "6":
        pgsci(band6)
    
    if str(freq_bands[i]) == "7":
        pgsci(band7)

    if str(freq_bands[i]) == "8":
        pgsci(band8)
     
    if str(freq_bands[i]) == "9":
        pgsci(band9)
    
    if str(freq_bands[i]) == "10":
        pgsci(band10)
    
    pgsfs(1)
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
    pgsci(1)
    pgsfs(2)
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
    name = sbname[i]
    i+=1


i=0 
pgsci(1)
pgsch(0.7)
x=0.6
while i < len(lststart):
#x = 0.5
    if lststart[i] > midnights[i]:
        pgtext(lststart[i], startday[i]+x -1, sbname[i])
#        x+=0.2
#        pgtext(lststart[i], startday[i]+x -1, 'Band '+str(freq_bands[i]))
    else:
        pgtext(lststart[i], startday[i]+x, sbname[i])
#        x+=0.2
#        pgtext(lststart[i], startday[i]+x, 'Band '+str(freq_bands[i]))
    
    i+=1

mns = []
mns.append(firstdaymnlst )
for x in midnights:
    mns.append(x)

mns.append(lastdaymnlst)
sd = []
sd.append(min(startday)-1.0)
for x in startday:
    sd.append(x)

sd.append( max(endday)+ 2.0)
        
x = array(mns)
y = array(sd)
pgline( x,y)

pgmtxt('B', 3, 1.0, 1.0, 'Vertical line is midnight civil time')

pgsci(band1)
pgmtxt('B', -8,1.0, 1.0, 'Freq.Band 1')
pgsci(band2)
pgmtxt('B', -7,1.0, 1.0, 'Freq.Band 2')
pgsci(band3)
pgmtxt('B', -6,1.0, 1.0, 'Freq.Band 3')
pgsci(band4)
pgmtxt('B', -5,1.0, 1.0, 'Freq.Band 4')
pgsci(band5)
pgmtxt('B', -4,1.0, 1.0, 'Freq.Band 5')
pgsci(band6)
pgmtxt('B', -3,1.0, 1.0, 'Freq. Band 6')
pgsci(band7)
pgmtxt('B', -2,1.0, 1.0, 'Freq. Band 7')
pgsci(band8)
pgmtxt('B', -1,1.0, 1.0, 'Freq. Band 8')
 
sim_start=""
sim_end=""
input_file = open(sys.argv[3],"r")
for x in input_file.readlines():
    if x.startswith("Simulation.beginTime"):
        sim_start = x
    if x.startswith("Simulation.endTime"):
        sim_end=x

pgsci(1)
pgmtxt('T', 2, 0.5, 0.5 ,str(sim_start))
pgmtxt('T', 1, 0.5, 0.5 ,str(sim_end))

pgend()
