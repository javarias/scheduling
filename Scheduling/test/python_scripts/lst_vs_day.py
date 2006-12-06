import ppgplot
from ppgplot import *
import sys
from math import floor
from math import ceil
from numarray import array

if len(sys.argv) < 4:
    print "\nScript Usage: "
    print "\tpython lst_vs_day.ps <stats filename> <graph output filename> <inputfile>"
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


#pgbeg("/xw",1,1)
pgbeg(sys.argv[2]+".ps/cps",1,1)
pgpap(11.0,0.5)
color =1
pgsci(color)
pgswin(0, 24, max(endday)+1.5, min(startday)-0.5)
pgbox('',0,0,'MV',1,0)
pgswin(0, 24, max(endday)+0.5, min(startday)-1.5)
pgbox('',0,0,'NV',1,0)
pglab("LST", "Calander Day", "Schedule")
pgswin(0, 24, max(endday)+ 2.0, min(startday)-1.0)
pgbox('BSCNT1',0,0,'BSCTV',1,0)

i =0
excep_color = 3
excel_color = 5
good_color = 6
ave_color = 12
bave_color = 7
poor_color = 8
dismal_color = 2
any_color = 4

while i < len(lststart):
    if str(weathernames[i]) == "exceptional":
        pgsci(excep_color)
    
    if str(weathernames[i]) == "excellent":
        pgsci(excel_color)
    
    if str(weathernames[i]) == "good":
        pgsci(good_color)
    
    if str(weathernames[i]) == "average":
        pgsci(ave_color)
    
    if str(weathernames[i]) == "belowaverage":
        pgsci(bave_color)
    
    if str(weathernames[i]) == "poor":
        pgsci(poor_color)
    
    if str(weathernames[i]) == "dismal":
        pgsci(dismal_color)
    
    if str(weathernames[i]) == "any":
        pgsci(any_color)
     
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
    i+=1


i=0 
pgsci(1)
pgsch(0.7)
x=0.2
while i < len(lststart):
    if x >= 0.8:
        x = 0.2
    else:
        x +=0.4
    if lststart[i] > midnights[i]:
        pgtext(lststart[i], startday[i]+x -1, sbname[i])
        x+=0.2
        pgtext(lststart[i], startday[i]+x -1, 'Band '+str(freq_bands[i]))
    else:
        pgtext(lststart[i], startday[i]+x, sbname[i])
        x+=0.2
        pgtext(lststart[i], startday[i]+x, 'Band '+str(freq_bands[i]))
    
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

pgsci(excep_color)
pgmtxt('B', -8,1.0, 1.0, 'Green is exceptional weather')
pgsci(excel_color)
pgmtxt('B', -7,1.0, 1.0, 'Light blue is excellent weahter')
pgsci(good_color)
pgmtxt('B', -6,1.0, 1.0, 'Magenta is good weahter')
pgsci(ave_color)
pgmtxt('B', -5,1.0, 1.0, 'Purple is average weahter')
pgsci(bave_color)
pgmtxt('B', -4,1.0, 1.0, 'Yellow is below average weahter')
pgsci(poor_color)
pgmtxt('B', -3,1.0, 1.0, 'Orange is poor weahter')
pgsci(dismal_color)
pgmtxt('B', -2,1.0, 1.0, 'Red is dismal weahter')
pgsci(any_color)
pgmtxt('B', -1,1.0, 1.0, 'Dark blue color is any weahter')
 
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
