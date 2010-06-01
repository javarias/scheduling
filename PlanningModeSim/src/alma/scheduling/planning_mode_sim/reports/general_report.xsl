<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">

  <!-- TODO: Add measure units to output reports -->
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
 
  <!-- Root template, also creates the body of the document -->
  <xsl:template match="/Results">
    <html>
      <head> <title>Planning Mode Simulator -- Report</title> </head>
      <body>

	<!-- General information -->
	<h1>Planning Mode Simulator -- Report</h1>
	
	<h2>Simulation parameters</h2>
	<ul>
	  <li><b>Simulation started on: </b> <xsl:value-of select="startRealDate"/></li>
	  <li><b>Simulation finished on: </b> <xsl:value-of select="stopRealDate"/></li>	
	  <li><b>Simulated observing seasons starts in: </b> <xsl:value-of select="obsSeasonStart"/></li>
	  <li><b>Simulated observing seasons ends in: </b> <xsl:value-of select="obsSeasonEnd"/></li>
    </ul>

        <h2>General Results</h2>
	<p> This section covers the execution results of the simulation.</p>
        <ul>
	  <li><b>ALMA's total available time: </b> <xsl:value-of select="availableTime"/> [sec]</li>
	  <li><b>Time spent in operations (maintenance + scientific): </b> <xsl:value-of select="operationTime"/> [hour]</li>
	  <li><b>Time spent in maintenance: </b> <xsl:value-of select="maintenanceTime"/> [hour]</li>
	  <li><b>Time spent in scientific observations: </b> <xsl:value-of select="scientificTime"/> [hour]</li>
        </ul>
	
	<!-- Array Section -->
	<xsl:if test='count(Array) > 0'>
	  <h2>Per Array Results</h2>
	    <xsl:apply-templates select="Array">
	      <xsl:sort select="id" data-type ="number" />
	    </xsl:apply-templates>
	</xsl:if>
	<xsl:if test='count(Array) = 0'>
	  <b>WARNING: </b> The simulation results file did not have any data on array configuration. Please check the simulation configuration (aprc-config.xml file). In case you suppose this should not happen, please contact the Scheduling subsystem team.
	</xsl:if>
	
	<!-- Observation Projects Section -->	
	<xsl:if test='count(ObservationProject) > 0'>
	  <h2>Per Observation Project Results</h2>
	  <xsl:apply-templates select="ObservationProject">
	    <xsl:sort select="originalId" data-type ="number" />
	  </xsl:apply-templates>
	</xsl:if>
	<xsl:if test='count(ObservationProject) = 0'>
	  <b>WARNING: </b> The simulation results file did not have any data on observation projects execution. Please check the simulation configuration. In case you suppose this should not happen, please contact the Scheduling subsystem team.
	</xsl:if>

	<!-- Scheduling Blocks Section -->	
	<xsl:if test='count(ObservationProject/SchedBlock) > 0'>
	  <h2>Per Scheduling Block Results</h2>
	  <xsl:apply-templates select="ObservationProject/SchedBlock" mode="general">
	    <xsl:sort select="originalId" data-type ="number" />
	  </xsl:apply-templates>
	</xsl:if>
	<xsl:if test='count(ObservationProject/SchedBlock) = 0'>
	  <b>WARNING: </b> The simulation results file did not have any data on scheduling blocks execution. Please check the simulation configuration. In case you suppose this should not happen, please contact the Scheduling subsystem team.
	</xsl:if>

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
	<li><b>Array's total available time: </b> <xsl:value-of select="availablelTime"/> [hour]</li>
	<li><b>Time spent in maintenance: </b> <xsl:value-of select="availablelTime"/> [hour]</li>
	<li><b>Time spent in scientific observations: </b> <xsl:value-of select="scientificTime"/> [hour]</li>
	<li><b>Resolution: </b><xsl:value-of select="resolution"/> [arcsec]</li>
	<li><b>UV Coverage: </b> <xsl:value-of select="uvCoverage"/></li>
      </ul>
  </xsl:template>

  <!-- Per observation project template -->
  <xsl:template match="ObservationProject">
    <h3>Observation Project ID <xsl:value-of select="originalId"/></h3>
      <ul>
		<li><b>Science Rank: </b> <xsl:value-of select="scienceRank"/></li>
		<li><b>Science Score: </b> <xsl:value-of select="scienceScore"/></li>
		<li><b>Execution Time: </b> <xsl:value-of select="executionTime"/> [hour]</li>
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
		  	<li>
		  		<xsl:for-each select="SchedBlock">
		    		 <a href="#SB-{originalId}"><xsl:value-of select="originalId"/></a>, 
		    	</xsl:for-each>
			</li>
		  </ul>
		</li>
      </ul>
  </xsl:template>

  <!-- Per scheduling block template -->
  <xsl:template match="ObservationProject/SchedBlock" mode="general">
    <a name="SB-{originalId}"/>
    <h3>Scheduling Block ID <xsl:value-of select="originalId"/></h3>
      <ul>
		<li><b>Started in: </b> <xsl:value-of select="startDate"/></li>
		<li><b>Ended in: </b> <xsl:value-of select="endDate"/></li>
		<li><b>Observation mode: </b> <xsl:value-of select="mode"/></li>
		<li><b>Type: </b> <xsl:value-of select="type"/></li>
		<li><b>Representative Frequency: </b> <xsl:value-of select="representativeFrequency"/></li>
		<li><b>Goal Sensitivity: </b> <xsl:value-of select="goalSensitivity"/></li>
		<li><b>Achieved Sensitivity: </b> <xsl:value-of select="achievedSensitivity"/></li>
		<li><b>Execution Time: </b> <xsl:value-of select="executionTime"/> [hour]</li>
		<li><b>Execution Status: </b> <xsl:value-of select="status"/></li>
		<li><b>Executed in Array: </b> <a href="#Array-{ArrayRef/@arrayRef}">Array <xsl:value-of select="ArrayRef/@arrayRef"/></a> </li>
      </ul>
  </xsl:template>


</xsl:stylesheet>
