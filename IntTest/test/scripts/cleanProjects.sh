#! /bin/bash

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
tmp=tmp_obsprep

# echo "Cleaning archive..."
# archiveCleanTest 1>>${tmp}/archiveCleanTest.out 2>> ${tmp}/archiveCleanTest.err
# 

echo "Loading archive schema..."
mkdir -p schemas
INTLIST_PATH=`echo $INTLIST | sed "s|:| |g"`
for directory in $ACSROOT $INTLIST_PATH $INTROOT; do
	for schema in ASDMBinaryTable.xsd ObsProject.xsd ObsReview.xsd ObsProposal.xsd ObsAttachment.xsd SchedBlock.xsd ProjectStatus.xsd; do
		find  ${directory}/idl -iname $schema -exec cp '{}' schemas \; ;
	done
done
archiveLoadSchema -u schemas 1>>${tmp}/archiveLoadSchema.out 2>>${tmp}/archiveLoadSchema.err
rm -rf schemas

exit 0
