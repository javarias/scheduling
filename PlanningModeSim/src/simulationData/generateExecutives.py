#!/usr/bin/env python

from users import users

#
# generateExecutives.py - Rafael Hiriart (rhiriart.nrao.edu)
#
# Generates a sample Executive data file, to be used in tests.
# It gets the input data from a list the participants to ADASS VI conference, in
# the users.py module.
# 

NORTH_AMERICA_EXEC = "NA"
EUROPE_EXEC = "EU"
ASIA_EXEC = "EA"
CHILE_EXEC = "CHILE"
OTHER_EXEC = "NONALMA"
countryToExecutive = {
    "USA" : NORTH_AMERICA_EXEC,
    "Canada" : NORTH_AMERICA_EXEC,
    "Russia" : EUROPE_EXEC,
    "Germany" : EUROPE_EXEC,
    "France" : EUROPE_EXEC,
    "Australia" : OTHER_EXEC,
    "Spain" : EUROPE_EXEC,
    "Netherlands" : EUROPE_EXEC,
    "Belgium" : EUROPE_EXEC,
    "Italy" : EUROPE_EXEC,
    "Scotland" : EUROPE_EXEC,
    "Denmark" : EUROPE_EXEC,
    "United Kingdom" : EUROPE_EXEC,
    "Sweden" : EUROPE_EXEC,
    "Ireland" : EUROPE_EXEC,
    "Ukraine" : EUROPE_EXEC,
    "China" : ASIA_EXEC,
    "Japan" : ASIA_EXEC,
    "Korea" : ASIA_EXEC,
    "Chile" : CHILE_EXEC
}

#
# XML template segments
#
executiveHeaderXmlTmpl = """<?xml version="1.0" encoding="UTF-8"?>
<ExecutiveData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
"""
executiveXmlTmpl = """    <Executive name="NA" defaultPercentage="60"/>
    <Executive name="EU" defaultPercentage="20"/>
    <Executive name="EA" defaultPercentage="10"/>
    <Executive name="CHILE" defaultPercentage="10"/>
"""
executiveSeasonHeaderXmlTmpl = """    <ObservingSeason name="EarlyScience2011" startDate="2011-01-01T00:00:00Z" endDate="2011-07-01T00:00:00Z">
"""
executiveSeasonPercentXmlTmpl = """        <ExecutivePercentage executiveRef="%execname%" percentage="%percent%"/>
"""
executiveSeasonFooterXmlTmpl = """      </ObservingSeason>
"""
executivePiXmlTmpl = """    <PI name="%name%" email="%email%">
        <PIMembership membershipPercentage="100" executiveRef="%execname%"/>
    </PI>
"""
executiveFooterXmlTmpl = """</ExecutiveData>
"""

#
# Create several structures based on the ADASS participant list 
#

# Executive percentages, based on how many participants from a country assisted to ADASS 
execpercent = {
    NORTH_AMERICA_EXEC : 0.0,
    EUROPE_EXEC : 0.0,
    ASIA_EXEC : 0.0,
    CHILE_EXEC : 0.0
}
for user in users:
    country = user[1]
    execpercent[countryToExecutive[country]] = execpercent[countryToExecutive[country]] + 1
for execname in execpercent.keys():
    execpercent[execname] = 100.0 * execpercent[execname] / len(users)

#
# Generate the XML file from the XML templates and the structures above
#

xml = executiveHeaderXmlTmpl
xml = xml + executiveXmlTmpl
xml = xml + executiveSeasonHeaderXmlTmpl
for execname in execpercent.keys():
    tmp = executiveSeasonPercentXmlTmpl
    tmp = tmp.replace("%execname%", execname)
    tmp = tmp.replace("%percent%", str(execpercent[execname]))
    xml = xml + tmp
xml = xml + executiveSeasonFooterXmlTmpl
for user in users:
    tmp = executivePiXmlTmpl
    tmp = tmp.replace("%name%", user[0])
    tmp = tmp.replace("%email%", user[2])
    tmp = tmp.replace("%execname%", countryToExecutive[user[1]])
    xml = xml + tmp
xml = xml + executiveFooterXmlTmpl

print xml
