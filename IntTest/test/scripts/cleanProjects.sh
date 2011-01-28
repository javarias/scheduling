#! /bin/bash

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
