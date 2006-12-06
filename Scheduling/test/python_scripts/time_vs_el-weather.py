import ppgplot
from ppgplot import *
from math import floor
from math import ceil
import sys

if len(sys.argv) <3:
    print "\nScript Usage: "
    print "\tplot3 <stats filename> <input filename>"
    print ""
    sys.exit()

weatherConstraintStrings = []
foo1 = sys.argv[2]
file1 = open(foo1,"r")
#file1 = open("../input1.txt","r")
for line in file1.readlines():
    if line.startswith("WeatherConstraints"):
        weatherConstraintStrings.append(line)

weatherConditionUsed = {}

foo2 = sys.argv[1]
file2 = open(foo2,"r")
#file2 = open("../stats_output.txt","r")
for line in file2.readlines():
    if not line.startswith("##"):
        l = line.split()
        if len(l) > 0:
            if not weatherConditionUsed.has_key( l[10].strip(',') ):
                weatherConditionUsed[l[10].strip(',')] = []
            weatherConditionUsed[l[10].strip(',')].append(l)

excep_color = 3
excel_color =5
good_color = 6
ave_color = 12
bave_color = 7
poor_color = 8
dismal_color = 2
any_color =4

for iter in weatherConditionUsed.iterkeys():
    if str(iter)== "exceptional":
        color = excep_color
    
    if str(iter) == "excellent":
        color =excel_color
    
    if str(iter) == "good":
        color =good_color
    
    if str(iter) == "average":
        color = ave_color
    
    if str(iter) == "belowaverage":
        color = bave_color
    
    if str(iter) == "poor":
        color = poor_color
    
    if str(iter) == "dismal":
        color = dismal_color
    
    if str(iter) == "any":
        color =any_color
     
    pgbeg(iter+".ps/cps", 1, 1)
    startTime = []
    endTime = []
    elevation = []
    for x in weatherConditionUsed[iter]:
        startTime.append(float(x[2].strip(',')))
        endTime.append(float(x[4].strip(',')))
        elevation.append(float(x[9].strip(',')))
        #print x[2].strip(',') +" "+ x[4].strip(',') +" "+ x[9].strip(',') 
    pgsvp(0.1, 0.9, 0.1, 0.9)
    last = len(startTime)-1
#pgswin(floor(startTime[0])-0.1,ceil(startTime[last])-0.1, 0, 2)
    pgswin(0,24,min(elevation) - 0.5, max(elevation)+0.5)
    pgbox('BSCNT1',0,0,'BSCNTV',0,0)
    i =0
    while i < len(startTime):
        pgsfs(1)
        pgsci(color) # color
        pgrect(startTime[i], endTime[i], elevation[i], elevation[i]+1.0)
        pgsci(1)
        pgsfs(2)
        pgrect(startTime[i], endTime[i], elevation[i], elevation[i]+1.0)
        i+=1
    i =0
    conditions = ""
    while i < len(weatherConstraintStrings):
        if weatherConstraintStrings[i].rfind(iter,0) != -1:
            conditions = weatherConstraintStrings[i]
        i+=1
    pglab("LST","Elevation","")


