#!/usr/bin/env python

import os
import sys

print "\n"
print "Weather Function: f(T) = p0*sin(t0*T + s0) + p1*cos(t1*T + s1) + p2"
print "T is time of day and calculated in simulator."
if len(sys.argv) != 8:
    print "Wrong number of input parameters"
    print "Usage: python ModifyWeatherParamters.py p0 p1 p2 s0 s1 t0 t1"
    sys.exit()

p0 = sys.argv[1]
p1 = sys.argv[2]
p2 = sys.argv[3]
s0 = sys.argv[4]
s1 = sys.argv[5]
t0 = sys.argv[6]
t1 = sys.argv[7]

filename1 = raw_input("Please enter a file name of the original input file: ") 
file1 = open("input.txt", "r")

filename2 = raw_input("Please enter a file name for new input file: ") 
file2 = open(filename2,"w")
        
filename3 = raw_input("Please enter a file name for the output file: ") 

space = ' ' 
cs = '; '
#Parse the existing file and create a new one, when reaching the weather.i
#line, it will replace it with the new parameters
for line in file1.readlines():
    if line[0:7] == "Weather" and line[0:23] != "Weather.numberFunctions":
        wl = line.split()
        new_wl = [wl[0], wl[1], wl[2], wl[3], sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6], sys.argv[7]]
        line = new_wl[0] +space+ new_wl[1] +space+ new_wl[2] +space+ new_wl[3] +space+ new_wl[4] +cs+ new_wl[5] +cs + new_wl[6] +cs+ new_wl[7] + cs + new_wl[8] +cs+ new_wl[9] +cs+ new_wl[10] 

    file2.write(line);
    file2.flush()
    
file2.close()
file1.close()

#Run the Simulator with the new file 
os.system("export CLASSPATH=$CLASSPATH:PMSimulator.jar:SchedulingInterfaces.jar;java alma.scheduling.PlanningModeSim.RunSimulator . "+filename2+" "+filename3+" log.txt &> runSim.log")

line = ""
file3 = open(filename3,"r") #output file opened for parsing
file4 = open("statistics_"+filename3,"w") #file with statistics pulled out

#put formula into statistics file and parameters entered 
file4.write("Weather Function: \n")
file4.write("\tf(T) = p0*sin(t0*T + s0) + p1*cos(t1*T + s1) + p2\n")
file4.write("\n")
file4.write("p0 = "+p0+"; p1 = "+p1+"; "+p2+"; s0 = "+s0+"; s1 = "+s1+"; t0 = "+t0+"; t1 = "+t1+"\n\n") 

#pull out output from the output file regarding the statistics
for line in file3.readlines():
    if line[0:21] == "Scheduling Statistics":
        file4.write(line+"\n")
    if line[0:21] == "Number of executions":
        file4.write(line)
    if line[0:10] == "Efficiency":
        file4.write(line)
    if line[0:19] == "Weighted Efficiency":
        file4.write(line)
    if line[0:12] == "% of science":
        file4.write(line)
    if line[0:10] == "Total time":
        file4.write(line)
    if line[0:18] == "Total science time":
        file4.write(line)
    if line[0:21] == "Possible science time":
        file4.write(line)
    if line[0:27] == "Number of scheduling blocks":
        file4.write(line)
    if line[0:7] == "Average":
        file4.write(line)

    file4.flush()
file4.close()
file3.close()     
print "Your statistics are in "+file4.name
