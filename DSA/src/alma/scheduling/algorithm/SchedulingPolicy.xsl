<?xml version="1.0" encoding="UTF-8"?>
<!--
ALMA - Atacama Large Millimeter Array
Copyright (c) AUI - Associated Universities Inc., 2011
(in the framework of the ALMA collaboration).
All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:util="http://www.springframework.org/schema/util"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:tx="http://www.springframework.org/schema/tx"
    xsi:schemaLocation="
    http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
    http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd
    http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd
    http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.0.xsd">
    
    <xsl:output encoding="UTF-8" method="xml" indent="yes"/>
    
    <xsl:template match="/Policies">
        <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:util="http://www.springframework.org/schema/util"
            xmlns:aop="http://www.springframework.org/schema/aop"
            xmlns:tx="http://www.springframework.org/schema/tx"
            xsi:schemaLocation="
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd
            http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd
            http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.0.xsd" 
            default-lazy-init="true" default-autowire="byName">
            <import resource="classpath:alma/scheduling/algorithm/DSAContext.xml"/>
        <xsl:for-each select="SchedulingPolicy">
            <xsl:if test="count(SelectionCriteria/WeatherSelector) = 1">
                <bean id="{concat('weatherTsysSelector', '_', @name)}" class="alma.scheduling.algorithm.weather.WeatherTsysSelector"
                    scope="prototype">
                    <constructor-arg><value>weatherTsysSelector</value></constructor-arg>
                    <property name="schedBlockDao" ref="sbDao"/>
                    <property name="tsysVariation" value="0.2"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriteria/ProjectCodeSelector) = 1">
                <bean id="{concat('projectCodeSelector', '_', @name)}" class="alma.scheduling.algorithm.obsproject.ProjectCodeSelector"
                    scope="prototype">
                    <constructor-arg><value>projectCodeSelector</value></constructor-arg>
                    <property name="prjDao" ref="obsProjectDao"/>
                    <property name="code" value="{SelectionCriteria/ProjectCodeSelector/code}" />
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriteria/ProjectGradeSelector) = 1">
                <bean id="{concat('projectGradeSelector', '_', @name)}" class="alma.scheduling.algorithm.obsproject.ProjectQualitySelector"
                    scope="prototype">
                    <constructor-arg><value>projectGradeSelector</value></constructor-arg>
                    <property name="prjDao" ref="obsProjectDao"/>
                    <property name="allowedGrades">
                        <set>
                        <xsl:for-each select="SelectionCriteria/ProjectGradeSelector/grade">
                            <value><xsl:value-of select="text()"/></value>
                        </xsl:for-each>
                        </set>
                    </property>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriteria/BandSelector) = 1">
            	<bean id="{concat('schedBlockBandSelector', '_', @name)}" class="alma.scheduling.algorithm.obsproject.BandSelector"
            		scope="prototype">
            		<constructor-arg><value>bandSelector</value></constructor-arg>
            		<property name="schedBlockDao" ref="sbDao"/>
            		<property name="allowedBands">
            			<set>
            			<xsl:for-each select="SelectionCriteria/BandSelector/band">
            				<value><xsl:value-of select="text()"/></value>
            			</xsl:for-each>
            			</set>
            		</property>
            	</bean>
            </xsl:if>
            
            <bean id="{concat('preUpdateSelector', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelector"
                scope="prototype">
                <property name="selectors">
                    <set>
                    	 <ref bean="sbStatusSelector" />
                    	 <ref bean="arrayTypeSelector" />
                        <xsl:if test="count(SelectionCriteria/ExecutiveSelector) = 1">
                            <ref bean="execSelector"/>
                        </xsl:if>
                            <ref bean="sourceSelector"/>
                        <xsl:if test="count(SelectionCriteria/ArrayConfigSelector) = 1">
                            <ref bean="arrayConfigSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/HourAngleSelector) = 1">
                            <ref bean="hourAngleSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/SunAvoidanceSelector) = 1">
                            <ref bean="sunAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/MoonAvoidanceSelector) = 1">
                            <ref bean="moonAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/SchedBlockGradeSelector) = 1">
                            <ref bean="projectQualitySelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/ArrayAngularResolutionSelector) = 1">
                            <ref bean="arrayAngularResolutionSelector" />
                        </xsl:if>   
                        	<ref bean="interactiveProjectsSelector" /> 
                        <xsl:if test="count(SelectionCriteria/ProjectCodeSelector) = 1">
                            <ref bean="{concat('projectCodeSelector', '_', @name)}" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/CSVProjectSelector) = 1">
                            <!-- For some unknown reason the 'boolean(SelectionCriteria/CSVProjectSelector/isCSV) = true()' doesn't work-->
                            <xsl:choose>
                                <xsl:when test="starts-with(SelectionCriteria/CSVProjectSelector/isCSV, 'true')">
                                    <ref bean="csvTrueSelector"/>
                                </xsl:when>
                                <xsl:when test="starts-with(SelectionCriteria/CSVProjectSelector/isCSV, '1')">
                                    <ref bean="csvTrueSelector"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <ref bean="csvFalseSelector"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/ProjectGradeSelector) = 1">
                            <ref bean="{concat('projectGradeSelector', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriteria/BandSelector) = 1">
                        	<ref bean="{concat('schedBlockBandSelector', '_', @name)}" />
                        </xsl:if>
                    </set>
                </property>
            </bean>
            
            <bean id="{concat('postUpdateSelectorAndUpdater', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelectorWithUpdater">
                <property name="selectors">
                    <set>
                    	<xsl:if test="count(SelectionCriteria/AltitudeSelector) = 1">
                    		<ref bean="altitudeSelector" />
                    	</xsl:if>
						<xsl:if test="count(SelectionCriteria/OpacitySelector) = 1">
							<ref bean="opacitySelector" />
						</xsl:if> 
                        <xsl:if test="count(SelectionCriteria/WeatherSelector) = 1">
                            <ref bean="{concat('weatherTsysSelector', '_', @name)}" />
                        </xsl:if>
                    </set>
                </property>
                <property name="partialUpdates">
                    <set>
                        <xsl:if test="/Policies/@sim='false' or count(/Policies/@sim)=0">
                            <ref bean="weatherUpdater"/>
                        </xsl:if>
                        <xsl:if test="/Policies/@sim='true'">
                            <ref bean="weatherSimUpdater"/>
                        </xsl:if>
                    </set>
                </property>
                <property name="fullUpdates">
                    <set>
                        <ref bean="sourceUpdater"/>
                    </set>
                </property>
            </bean>
            
            <xsl:if test="count(Scorers/SciScorer) = 1">
                <xsl:variable name="weight" select="string(Scorers/SciScorer/weight)"/>
                <bean id="{concat('sciScorer', '_', @name)}" class="alma.scheduling.algorithm.sbranking.ScienceGradeRanker" scope="prototype">
                    <constructor-arg><value>sciScorer</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                <xsl:variable name="weight" select="Scorers/HourAngleScorer/weight"/>
                <bean id="{concat('hourAngleScorer', '_', @name)}" class="alma.scheduling.algorithm.obsproject.HourAngleRanker" scope="prototype">
                    <constructor-arg><value>hourAngleScorer</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/TsysScorer) = 1">
                <xsl:variable name="weight" select="Scorers/TsysScorer/weight"/>
                <bean id="{concat('TsysScorer', '_', @name)}" class="alma.scheduling.algorithm.weather.TsysScorer" scope="prototype">
                    <constructor-arg><value>TsysScorer</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/ExecutiveBalancingScorer) = 1">
                <xsl:variable name="weight" select="Scorers/ExecutiveBalancingScorer/weight"/>
                <bean id="{concat('executiveBalancingScorer', '_', @name)}" class="alma.scheduling.algorithm.executive.ExecutiveBalancingScorer" scope="prototype">
                    <constructor-arg><value>executiveBalancingScorer</value></constructor-arg>
                    <property name="execBalance">
                        <map>
                        <xsl:for-each select="Scorers/ExecutiveBalancingScorer/Executive">
                            <entry key="{@name}" value="{@value}"/>
                        </xsl:for-each>
                         </map>
                    </property>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <bean id="{concat('finalRanker', '_', @name)}" class="alma.scheduling.algorithm.sbranking.FinalRanker" scope="prototype">
                <constructor-arg><value>finalRanker</value></constructor-arg>
                <property name="rankers">
                    <set>
                        <xsl:if test="count(Scorers/SciScorer) = 1">
                            <ref bean="{concat('sciScorer', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                            <ref bean="{concat('hourAngleScorer', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/TsysScorer) = 1">
                            <ref bean="{concat('TsysScorer', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/ExecutiveBalancingScorer) = 1" >
                        	<ref bean="{concat('executiveBalancingScorer', '_', @name)}"/>
                        </xsl:if>
                    </set>
                </property>
                <property name="weights">
                    <list>
                    </list>
                </property>
            </bean>
            
            <bean id="{@name}" class="alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl"
                scope="prototype">
                <property name="ranker" ref="{concat('finalRanker', '_', @name)}"/>
                <property name="preUpdateSelectors">
                    <set>
                        <ref bean="{concat('preUpdateSelector', '_', @name)}"/>
                    </set>
                </property>
                <property name="postUpdateSelectors">
                    <set>
                        <ref bean="{concat('postUpdateSelectorAndUpdater', '_', @name)}"/>
                    </set>
                </property>
                
                <property name="firstRunUpdaters">
                    <set>
                        <ref bean="sourceUpdater"/>
                    </set>
                </property>
                <property name="updaters">
                    <set>
                        <ref bean="{concat('postUpdateSelectorAndUpdater', '_', @name)}"/>
                    </set>
                </property>
            </bean>
        </xsl:for-each>
        </beans>
    </xsl:template>
    
</xsl:stylesheet>
