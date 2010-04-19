#!/usr/bin/env python
#
# generateProject.py - Rafael Hiriart (rhiriart@nrao.edu)
#
# Generates ObsProjects with parameters following suitable distributions.
#

import math
import random
from users import users
from sensitivity import pointSourceSensitivity

###############################################################################
# 
#

# Number of SchedBlocks that this script will generate
nschedblks = 7000

pis = []
for user in users:
    pis.append(user[2])

#
# RA, Dec distribution, uniformly distributed in
# 0.0 <= RA <= 360.0
# -90.0 <= Dec <= 47.0
#
deltaRA = 1.0
startRA = 0.0
endRA = 360.0
deltaDec = 1.0
startDec = -90.0
endDec = 47.0
K = 10

points = []
ra = startRA
while ra <= (endRA - deltaRA):
    dec = startDec
    while dec <= (endDec - deltaDec):
        npoints = int( K * deltaRA * deltaDec * math.cos(math.radians(dec)) )
        for i in range(npoints):
            pra = ra + random.random() * deltaRA
            pdec = dec + random.random() * deltaDec 
            points.append((pra, pdec))
        dec = dec + deltaDec
    ra = ra + deltaRA

#
# Receiver band distribution
#

# Receiver band limits (GHz)
bandLimits = { 'band3' : (84.0, 116.0),
               'band4' : (125.0, 163.0),
               'band5' : (163.0, 211.0),
               'band6' : (211.0, 275.0),
               'band7' : (275.0, 373.0),
               'band8' : (385.0, 500.0),
               'band9' : (602.0, 720.0) }
# Receiver band distribution
bandPercent = { 'band3' : 0.26,
                'band4' : 0.018,
                'band5' : 0.015,
                'band6' : 0.33,
                'band7' : 0.221,
                'band8' : 0.078,
                'band9' : 0.078 }
# Frequency distribution
numFreqs = 1000
freqs = []
for band in bandPercent.keys():
    nfreq = int( bandPercent[band] * numFreqs )
    for i in range(nfreq):
        f = bandLimits[band][0] + random.random() * ( bandLimits[band][1] - bandLimits[band][0] )
        freqs.append(f)

#
# Observation time distribution
#
times = []
# distribution shape parameters (hours)
t1 = 6.0    # 3-8 hours
d1 = 10.0
t2 = 30.0
d2 = 5.0
tmax = 40.0    # 40-50 hours
deltat = 0.1
t = 0
while t < t1:
   n = (d1/t1)*t 
   for i in range(int(n)):
       times.append(t + random.random() * deltat)
   t = t + deltat
while t < t2:
   n = d1 + (t-t1)*(d2-d1)/(t2-t1)
   for i in range(int(n)):
       times.append(t + random.random() * deltat)
   t = t + deltat
while t < tmax:
   n = d2 - d2*(t-t2)/(tmax-t2)
   for i in range(int(n)):
       times.append(t + random.random() * deltat)
   t = t + deltat

#
# Array requested distribution
#
ACA = 0.25
BL = 1.0 - ACA
arrayRequestedDist = []
for i in range(int(100*ACA)): arrayRequestedDist.append("ACA")
for i in range(int(100*BL)): arrayRequestedDist.append("TWELVE_M")

#
# Sensitivity goal
#
def sensitivity(freq, obsTime, decl):
    exposureTime = 0.5 * obsTime
    bandwidth = 4.0
    numberAntennas = 50
    antennaDiameter = 12.0
    latitude = -23.0
    opacity = 0.2
    atmBrightnessTemp = 0.26
    return pointSourceSensitivity(exposureTime, freq, bandwidth, decl, 
        numberAntennas, antennaDiameter, latitude, opacity, atmBrightnessTemp)

#
# Science score and rank
#
scores = []
min_score = 1.0
max_score = 7.0
for i in range(nschedblks):
    scores.append(random.uniform(min_score, max_score))
scores.sort()

prjHeader = """<?xml version="1.0" encoding="UTF-8"?>
<ObsProject xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <PrincipalInvestigator>%piid%</PrincipalInvestigator>
    <ScientificScore>%score%</ScientificScore>
    <ScientificRank>%rank%</ScientificRank>
    <ObsUnitSet>
"""

prjFooter = """    </ObsUnitSet>
</ObsProject>
"""

sbTemplate = """        <SchedBlock>
            <WeatherConstraints maxWindVelocity="0.0" maxOpacity="0.0" minPhaseStability="0.0"
                maxSeeing="0.0"/>
            <SchedulingConstraints representativeTargetIdRef="T1" maxAngularResolution="%resolution%" representativeFrequency="%freq%"/>
            <Preconditions maxAllowedHA="2.0" minAllowedHA="-2.0"/>
            <ObsParameters id="OP1">
                <ScienceParameters duration="180.0" representativeBandwidth="4.0"
                    representativeFrequency="%freq%" sensitivityGoal="%sensitivity%"/>
            </ObsParameters>
            <InstrumentSpec id="SS1">
                <SpectralSpec/>
            </InstrumentSpec>
            <Target id="T1" sourceIdRef="S1" instrumentSpecIdRef="SS1"/>
            <FieldSource id="S1" name="%srcname%" RA="%ra%" Dec="%dec%" pmRA="0.0" pmDec="0.0"/>
            <SchedBlockControl>
                <MaximumTime>%maxtime%</MaximumTime>
                <EstimatedExecutionTime>%exectime%</EstimatedExecutionTime>
                <ArrayRequested>%array_requested%</ArrayRequested>
                <IndefiniteRepeat>false</IndefiniteRepeat>
            </SchedBlockControl>
        </SchedBlock>
"""

def createObsProjectFile(i, params):
    sbTime = 0.5
    fileName = "GenObsProject%04d.xml" % i
    file = open(fileName, 'w')
    srcName = "%.4f-%.3f" % (params[0][0], params[0][1])
    prjxml = prjHeader
    prjxml = prjxml.replace("%piid%", pis[random.randint(0, len(pis)-1)])
    prjxml = prjxml.replace("%score%", str(params[6]))
    prjxml = prjxml.replace("%rank%", str(int(params[7])))
    sbxml = sbTemplate
    sbxml = sbxml.replace("%srcname%", srcName) 
    sbxml = sbxml.replace("%ra%", str(params[0][0])) 
    sbxml = sbxml.replace("%dec%", str(params[0][1])) 
    sbxml = sbxml.replace("%freq%", str(params[1]))
    sbxml = sbxml.replace("%maxtime%", str(params[2]))
    sbxml = sbxml.replace("%exectime%", str(sbTime))
    sbxml = sbxml.replace("%sensitivity%", str(params[4]))
    sbxml = sbxml.replace("%resolution%", str(params[3]))
    sbxml = sbxml.replace("%array_requested%", str(params[5]))
    # prjTime =  params[2]
    # while prjTime > 0:
    #     prjxml = prjxml + sbxml
    #     prjTime = prjTime - 0.5
    prjxml = prjxml + sbxml
    prjxml = prjxml + prjFooter
    file.write(prjxml)
    file.close()

# createObsProjectFile(1, sbparams[0])

#
# SchedBlock generation
#  

# List of tuples with SB parameters
#   0 - tuple with RA, Dec coordinates (degrees, degrees)
#   1 - frequency (GHz)
#   2 - project total observation time (hours)
#   3 - resolution (arcseconds)
#   4 - sensitivity goal (Jy)
# sbparams = []
for i in range(nschedblks):
    src_coords = points[random.randint(0, len(points)-1)]
    freq = freqs[random.randint(0, len(freqs)-1)]
    obsTime = times[random.randint(0, len(times)-1)]
    resolution = random.uniform(0.001, 3.0)
    arrayReq = arrayRequestedDist[random.randint(0, len(arrayRequestedDist)-1)]
    # sbparams.append((src_coords, freq, obsTime, resolution, sensitivity(freq, obsTime)))
    createObsProjectFile(i, (src_coords, freq, obsTime, resolution, sensitivity(freq, obsTime, src_coords[1]), arrayReq, scores[i], i))

# print sbparams
