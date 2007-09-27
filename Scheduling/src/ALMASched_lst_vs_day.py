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

weathertype = sys.argv[5]
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

for line in file1.readlines():
    if not line.startswith("#"):
        l = line.split()
        if len(l) > 0:
            sbname.append(l[0].strip(','))
            lststart.append(float(l[2].strip(',')))
            execlen.append(float(l[4].strip(',')))
            startday.append(float(l[13].strip(',')))
            endday.append(float(l[14].strip(',')))
            midnights.append(float(l[15].strip(',')))
            firstdaymnlst =(float(l[16].strip(',')))
            lastdaymnlst =(float(l[17].strip(',')))
            weathernames.append(l[10].strip(','))
            freq_bands.append(int(l[6].strip(',')))
            startmonth.append(int(l[18].strip(',')))
            endmonth.append(int(l[19].strip(',')))

if max(startmonth) != max(endmonth):
    print "This script is not currently designed for schedules spanning more than one month"
    sys.exit()

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

# number of days in each month
jan_d = 31
feb_d = 28
mar_d = 31
apr_d = 30
may_d = 31
jun_d = 30
jul_d = 31 
aug_d = 31
sep_d = 30
oct_d = 31 
nov_d = 30
dec_d = 31

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

#if within the same month e_idx will not change here    
while e_idx < len(wmon):
    if not str(wmon[e_idx]) == emon:
        e_idx = e_idx+1
    else:
        break
# and then we need the index of the end day, starting from the end 
# month index
#just doing as much as the end day, if we go all the way to the  end of the graph
# problems might (have in the past) occur
eday = max(endday)
while e_idx < len(wday):
    if not float(wday[e_idx]) == eday:
        e_idx += 1
    else:
        break

t_idx =e_idx
while float(wday[t_idx+1]) == float(wday[e_idx]):
    t_idx +=1

#pgbeg("/xw",1,1)
pgbeg(sys.argv[2]+".ps/cps",1,1) 
pgpap(10.5,0.75)
pgsvp(0.1, 0.8, 0.1, 0.9)   
color =1
pgsci(color)
#left side labelling: puts the start day / end day +1 labels in proper position
pgswin(0, 24, max(endday)+0.5, min(startday)-1.5)
pgbox('',0,0,'NV',1,0)
#right side labelling: puts the start day / end day +1 labels in proper position
pgswin(0, 24, max(endday)+1.5, min(startday)-0.5)
pgbox('',0,0,'MV',1,0)
pglab("LST", "Calander Day", "Schedule")
#puts the ticks on the left and right side in proper position
#they are positioned under the number, not in the middle
pgswin(0, 24, max(endday)+2.0, min(startday)-1.0)
pgbox('BSCNT1',0,0,'BSCTV',1,0)

#pgsci(0)    
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
while i < (t_idx+1):
# Get color associated with value
    pgsfs(1)
    pgsci(getcolor(float(wval[i])))
    
    if (i+1) >= len(wtime):
        break;
    elif float(wtime[i+1]) > float(wtime[i]): #working on same day
        pgrect(float(wtime[i]), float(wtime[i+1]), float(wday[i]), float(wday[i])+1.0)
    else: # wtime[i+1] is less than wtime[i] 
        pgrect(float(wtime[i]), 24.0,
            float(wday[i]), float(wday[i])+1.0)
        pgrect(0.0, float(wtime[i+1]),
            float(wday[i])+1.0, float(wday[i])+2.0)
#    if wday[i] == '02' and float(wtime[i]) == 0.017:
#        print 'hmm'
#        pgsci(0)
#        pgrect(float(wtime[i]), float(wtime[i+1]), float(wday[i])-1.0, float(wday[i]))
        
    i += 1

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
pgsci(0)
while i < len(lststart):
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
    if lststart[i] > midnights[i]:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i]-1, startday[i])
    else:
        pgrect( lststart[i], lststart[i]+execlen[i], startday[i], startday[i]+1)
    name = sbname[i]
    i+=1


i=0 
pgsci(0)
pgsch(0.6)
x=0.6
while i < len(lststart):
    if lststart[i] > midnights[i]:
        pgtext(lststart[i], startday[i]+x -1, sbname[i])
#        pgtext(lststart[i], startday[i]+x -1, 'Band '+str(freq_bands[i]))
    else:
        pgtext(lststart[i], startday[i]+x, sbname[i])
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

pgsci(0)
pgmtxt('T', 2, 0.5, 0.5 ,str(sim_start))
pgmtxt('T', 1, 0.5, 0.5 ,str(sim_end))

#draws legend for what color maps to which value
makecolorwedge()      
pgend()

