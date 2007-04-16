#!/usr/bin/env python
import ppgplot
from ppgplot import *
import sys
from math import log10
from numarray import array
import time
import string


if len(sys.argv) < 6:
    print "\nScript Usage: "
    print "\tALMASched_lst_vs_day.py <stats filename> <graph output filename> <inputfile> <weatherfile> <weatherType: wind/opacity/rms>"
    print ""
    sys.exit()

    

    
#colors for different frequency bands
weathertype = sys.argv[5]
#for each SB.
lststart= []
startmonth=[]
startday = []
endmonth=[]
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
            startmonth.append(int(l[19].strip(',')))
            endmonth.append(int(l[20].strip(',')))

#Automatically display results
#pgbeg("/xw",1,1)
#Save to color post script file            
pgbeg(sys.argv[2]+".ps/cps",1,1) 
#Save to gif file            
foo = "_tmp_schedule.gif"   
pgbeg(foo+"/GIF",1,1)
pgpap(10.5,0.75)
pgsvp(0.1, 0.8, 0.1, 0.9)   
color =1
pgsci(color)
#left side labelling
#pgswin(0, 24, 31+0.5, 0-0.5);

wmon=[]
wday = []
wtime=[]
wval = []
weatherFile = open(sys.argv[4],"r")
for line in weatherFile.readlines():
    if not line.startswith("#"):
        l = line.split()
        if len(l) > 0:
            wmon.append(l[1].strip())
            wday.append(l[2].strip())
            wtime.append(l[3].strip())
            wval.append(l[4].strip())

# First get index of occurance of start month in wmon
s_idx = 0 # will end up to be the index of the start month
if startmonth[0] < 10:
    smon = "0"+str(startmonth[0])
else:        
    smon = str(startmonth[0])
           
while s_idx < len(wmon):
    if not str(wmon[s_idx]) == smon:
        s_idx= s_idx+1
    else:
        break
        
# Then we need to get index of start day in wday, starting from s_idx 
# because that is where the month started, indices all correspond in the
# w-arrays
sday = str(startday[0])
while s_idx < len(wday):
    if not str(float(wday[s_idx])) == sday:
        s_idx = s_idx +1
    else:
        break
# now we set the end index to the start index and we look for the 
# end month index
e_idx =s_idx #index of last thing
if endmonth[0] < 10:
    emon = "0"+str(endmonth[len(endmonth)-1])
else:        
    emon = str(endmonth[len(endmonth)-1])
#print emon
while e_idx < len(wmon):
    if not str(wmon[e_idx]) == emon:
        e_idx = e_idx+1
    else:
        break
# and finally we need the index of the end day, starting from the end 
# month index
#adding 2 to end day because thats what we've added to our graph
eday = max(endday)+2
while e_idx < len(wday):
    if not float(wday[e_idx]) == eday:
        e_idx = e_idx+1
    else:
        break

pgswin(0, 24, max(endday)+1.5, min(startday)-1.5)
#right side labelling
pgswin(0, 24, max(endday)+2.5, min(startday)-0.5)
pglab("LST", "Calander Day", "Schedule")
pgswin(0, 24, max(endday)+2.0, min(startday)-1.0)

#create the color mapping somehow related to the values of the weather
valsubset = wval[s_idx:e_idx]
maxOP = max(valsubset)
minOP = min(valsubset) 
(cmin, cmax) =pgqcir()
for i in range (cmin, cmax+1):
   pgshls(i, (360/(cmax-cmin))*(i-cmin), 0.5, 1.0)
    
pgsfs(1)
def getcolor(data):
    return int(( (cmax - cmin)/ (float(maxOP) - float(minOP))) * (data - float(minOP))+cmin)

i=s_idx
#i=0
pgscr(1,1,1,1)
pgscr(0,0,0,0)    
while i < (e_idx+1):
#while i < len(valsubset):
# Get color associated with value
    pgsfs(1)
    pgsci(getcolor(float(wval[i])))
#pgsci(getcolor(float(valsubset[i])))
    if float(wtime[i+1]) > float(wtime[i]): 
#     print 'first case'
        if not float(wday[i]) < 10.0:
            pgrect(float(wtime[i]), float(wtime[i+1]), 
                float(wday[i].strip('0'))-1.0, float(wday[i].strip('0')))
        else:
            pgrect(float(wtime[i]), float(wtime[i+1]),
                    float(wday[i])-1.0, float(wday[i]))
    else: # wtime[i+1] is less than wtime[i]
        if not float(wday[i]) < 10.0:
            pgrect(float(wtime[i]), 24.0,
                float(wday[i].strip('0'))-1.0, float(wday[i].strip('0')))
            pgrect(0.0, float(wtime[i+1]),
                float(wday[i].strip('0')), float(wday[i].strip('0'))+1.0)
        else:
            pgsci(1)
            pgrect(float(wtime[i]), 24.0,
                    float(wday[i])-1.0, float(wday[i]))
            pgrect(0.0, float(wtime[i+1]),
                    float(wday[i]), float(wday[i])+1.0)
#   pgsfs(2)
#    pgsci(0)
#    if not wtime[i+1] < wtime[i]: 
#        if not float(wday[i]) < 10.0:
#            pgrect(float(wtime[i]), float(wtime[i+1]), 
#                float(wday[i].strip('0'))-1.0, float(wday[i].strip('0')))
#        else:
#            pgrect(float(wtime[i]), float(wtime[i+1]),
#                float(wday[i])-1.0, float(wday[i]))
    i = i+1
    
pgsci(0)    

pgbox('',0,0,'NV',1,0)
pgbox('',0,0,'MV',1,0)
pgbox('BSCNT1',0,0,'BSCTV',0,0)
def makecolorwedge():    
    pgsfs(1)
    pgsvp(0.95, 0.97, 0.2, 0.8)
    pgswin(0, 1, cmin-0.5, cmax+0.5)
    for i in range(cmin, cmax+1):  
        pgsci(i)
        pgrect(0,1,i-0.5, i+0.5)
    pgsci(0)    
    pgswin(0,1,float(minOP), float(maxOP))
    pgbox('BC',0,0,'BCNTSV',0,0)  

i =0
    
pgsfs(2)
while i < len(lststart):
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
# pgsci(0)
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
    name = sbname[i]
    i+=1


i=0 
#pgsci(0)
pgsch(0.7)
x=0.6
while i < len(lststart):
#x = 0.5
    if x >= 0.8:
        x = 0.2
    else:
        x +=0.4
    if lststart[i] > midnights[i]:
        pgtext(lststart[i], startday[i]+x -1, sbname[i])
        x+=0.2
#        pgtext(lststart[i], startday[i]+x -1, 'Band '+str(freq_bands[i]))
    else:
        pgtext(lststart[i], startday[i]+x, sbname[i])
        x+=0.2
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
pgmtxt('B', 4, 1.0, 1.0, 'Background shows '+weathertype)

sim_start=""
sim_end=""
input_file = open(sys.argv[3],"r")
for x in input_file.readlines():
    if x.startswith("Simulation.beginTime"):
        sim_start = x
    if x.startswith("Simulation.endTime"):
        sim_end=x

#pgsci(0)
pgmtxt('T', 2, 0.5, 0.5 ,str(sim_start))
pgmtxt('T', 1, 0.5, 0.5 ,str(sim_end))

makecolorwedge()
pgend()
