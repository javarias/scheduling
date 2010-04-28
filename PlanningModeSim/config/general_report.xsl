<!--
ALMA - Atacama Large Millimeter Array
(c) European Southern Observatory, 2002
(c) Associated Universities Inc., 2002
Copyright by ESO (in the framework of the ALMA collaboration),
Copyright by AUI (in the framework of the ALMA collaboration),
All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY, without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston,
MA 02111-1307  USA
-->

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">

  <!-- TODO: Add measure units to output reports -->
  <!-- TODO: Add hyperlinks between elements-->
 
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
 
  <!-- Root template, also creates the body of the document -->
  <xsl:template match="/Results">
    <html>
      <head> <title>Planning Mode Simulator -- Report</title> </head>
      <body>
	<h1>Planning Mode Simulator -- Report</h1>
	
	<h2>Simulation parameters</h2>
	<ul>
	  <li><b>Simulated observing seasons starts in: </b> <xsl:value-of select="obsSeasonStart"/></li>
	  <li><b>Simulated observing seasons ends in: </b> <xsl:value-of select="obsSeasonEnd"/></li>
        </ul>

        <h2>General Results</h2>
	<p> This section covers the execution results of the simulation.</p>
        <ul>
	  <li><b>ALMA's total available time: </b> <xsl:value-of select="availableTime"/> [sec]</li>
	  <li><b>Time spent in operations (maintenance + scientific): </b> <xsl:value-of select="operationTime"/> [sec]</li>
	  <li><b>Time spent in maintenance: </b> <xsl:value-of select="maintenanceTime"/> [sec]</li>
	  <li><b>Time spent in scientific observations: </b> <xsl:value-of select="scientificTime"/> [sec]</li>
        </ul>

	<h2>Per Array Results</h2>
	<xsl:apply-templates select="Array">
	  <xsl:sort select="id" data-type ="number" />
	</xsl:apply-templates>

	<h2>Per Observation Project Results</h2>
	<xsl:apply-templates select="ObservationProject">
	  <xsl:sort select="id" data-type ="number" />
	</xsl:apply-templates>

	<h2>Per Scheduling Block Results</h2>
	<xsl:apply-templates select="ObservationProject/SchedBlock">
	  <xsl:sort select="id" data-type ="number" />
	</xsl:apply-templates>

      </body>
    </html>
  </xsl:template>

  <!-- Per array template -->
  <xsl:template match="Array">
    <a name="Array-{@id}"/>
    <h3>Array ID <xsl:value-of select="@id"/></h3>
      <ul>
	<li><b>Array created in: </b> <xsl:value-of select="creationDate"/></li>
	<li><b>Array deleted in: </b> <xsl:value-of select="deletionDate"/></li>
	<li><b>Array's total available time: </b> <xsl:value-of select="availablelTime"/> [sec]</li>
	<li><b>Time spent in maintenance: </b> <xsl:value-of select="availablelTime"/> [sec]</li>
	<li><b>Time spent in scientific observations: </b> <xsl:value-of select="scientificTime"/> [sec]</li>
	<li><b>Resolution: </b><xsl:value-of select="resolution"/> [arcsec]</li>
	<li><b>UV Coverage: </b> <xsl:value-of select="uvCoverage"/></li>
      </ul>
  </xsl:template>

  <xsl:template match="ObservationProject">
    <h3>Observation Project ID <xsl:value-of select="id"/></h3>
      <ul>
	<li><b>Science Rank: </b> <xsl:value-of select="scienceRank"/></li>
	<li><b>Science Score: </b> <xsl:value-of select="scienceScore"/></li>
	<li><b>Execution Time: </b> <xsl:value-of select="executionTime"/></li>
	<li><b>Execution Status: </b> <xsl:value-of select="status"/></li>
	<li><b>List of Affiliation: </b>
	  <ul>
	    <xsl:for-each select="Affiliation">
	    <li><b><xsl:value-of select="executive"/>: </b> <xsl:value-of select="percentage"/>%</li>
	    </xsl:for-each>
	  </ul>	
	</li>
	<li><b>List of Scheduling Blocks </b>
	  <ul>
	    <xsl:for-each select="SchedBlock">
	    <a href="#SB-{id}"><xsl:value-of select="id"/></a>, 
	    </xsl:for-each>
	  </ul>	
	</li>

      </ul>
  </xsl:template>

  <xsl:template match="ObservationProject/SchedBlock">
    <a name="SB-{id}"/>
    <h3>Scheduling Block ID <xsl:value-of select="id"/></h3>
      <ul>
	<li><b>Started in: </b> <xsl:value-of select="startDate"/></li>
	<li><b>Ended in: </b> <xsl:value-of select="endDate"/></li>
	<li><b>Observation mode: </b> <xsl:value-of select="mode"/></li>
	<li><b>Type: </b> <xsl:value-of select="type"/></li>
	<li><b>Representative Frequency: </b> <xsl:value-of select="representativeFrequency"/></li>
	<li><b>Execution Time: </b> <xsl:value-of select="executionTime"/></li>
	<li><b>Execution Status: </b> <xsl:value-of select="status"/></li>
	<li><b>Executed in Array: </b> <a href="#Array-1">Array 1</a> </li>
      </ul>
  </xsl:template>

</xsl:stylesheet>
