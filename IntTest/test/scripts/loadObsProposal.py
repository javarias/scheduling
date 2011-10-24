#!/usr/bin/env python

#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) AUI - Associated Universities Inc., 2011
# (in the framework of the ALMA collaboration).
# All rights reserved.
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************
import sys
import zipfile
import re
from glob import glob
from Acspy.Clients.SimpleClient import PySimpleClient
from xmlentity import XmlEntityStruct

def replaceUIDs(xml, obsProposalUID, obsProjectUID, schedBlockUIDs):
    xml = xml.replace("%ObsProposalEntityUID%", obsProposalUID)
    xml = xml.replace("%ObsProjectEntityUID%", obsProjectUID)
    for i in range(len(schedBlockUIDs)):
        xml = xml.replace("%SchedBlockEntityUID"+str(i)+"%", schedBlockUIDs[i])
    return xml

client = PySimpleClient()
archconn = client.getComponent('ARCHIVE_CONNECTION')
archop = archconn.getOperational('loadObsProposal')
archid = client.getComponent('ARCHIVE_IDENTIFIER')

path = sys.argv[1]
aotfile = sys.argv[1]
zf = zipfile.ZipFile(aotfile)
files = zf.namelist()

if 'ObsProposal.xml' not in files:
    exit('file %s doesn\'t contain %s' % (aotfile, 'ObsProposal.xml'))
else:
    xmlObsProposal = zf.read('ObsProposal.xml')

if 'ObsProject.xml' not in files:
    exit('file %s doesn\'t contain %s' % (aotfile, 'ObsProject.xml'))
else:
    xmlObsProject = zf.read('ObsProject.xml')
xmlSchedBlocks = []
for fn in files:
    if re.match('SchedBlock.*xml', fn) is not None:
        xmlSchedBlocks.append(zf.read(fn))

numUIDs = len(xmlSchedBlocks) + 2
uids = archid.getUIDs(numUIDs)
sbUIDs = ()
for i in range(2, len(uids)):
    sbUIDs = sbUIDs + (uids[i],)
uidm = {'ObsProposal' : uids[0], 'ObsProject' : uids[1], 'SchedBlock' : sbUIDs}

entity = XmlEntityStruct(replaceUIDs(xmlObsProposal, uidm['ObsProposal'], uidm['ObsProject'], uidm['SchedBlock']),
                         uidm['ObsProposal'], 'ObsProposal', '1.0', '0')
archop.store(entity)
entity = XmlEntityStruct(replaceUIDs(xmlObsProject, uidm['ObsProposal'], uidm['ObsProject'], uidm['SchedBlock']),
                         uidm['ObsProject'], 'ObsProject', '1.0', '0')
archop.store(entity)
for i in range(len(xmlSchedBlocks)):
    entity = XmlEntityStruct(replaceUIDs(xmlSchedBlocks[i], uidm['ObsProposal'], uidm['ObsProject'], uidm['SchedBlock']),
                         uidm['SchedBlock'][i], 'SchedBlock', '1.0', '0')
    archop.store(entity)

client.releaseComponent('ARCHIVE_CONNECTION')
client.releaseComponent('ARCHIVE_IDENTIFIER')
client.disconnect()

