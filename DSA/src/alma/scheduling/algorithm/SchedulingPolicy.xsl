<?xml version="1.0" encoding="UTF-8"?>
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
            <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                <bean id="{concat('weatherTsysSelector', '_', @name)}" class="alma.scheduling.algorithm.weather.WeatherTsysSelector"
                    scope="prototype">
                    <constructor-arg><value>weatherTsysSelector</value></constructor-arg>
                    <property name="schedBlockDao" ref="sbDao"/>
                    <property name="tsysVariation" value="0.2"/>
                </bean>
            </xsl:if>
            
            <bean id="{concat('preUpdateSelector', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelector"
                scope="prototype">
                <property name="selectors">
                    <set>
                        <xsl:if test="count(SelectionCriterion/ExecutiveSelector) = 1">
                            <ref bean="execSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SourceSelector) = 1">
                            <ref bean="sourceSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/ArrayConfigSelector) = 1">
                            <ref bean="arrayConfigSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/HourAngleSelector) = 1">
                            <ref bean="hourAngleSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SunAvoidanceSelector) = 1">
                            <ref bean="sunAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/MoonAvoidanceSelector) = 1">
                            <ref bean="moonAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SchedBlockGradeSelector) = 1">
                            <ref bean="sbStatusSelector" />
                        </xsl:if>
                    </set>
                </property>
            </bean>
            
            <bean id="{concat('postUpdateSelectorAndUpdater', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelectorWithUpdater">
                <property name="selectors">
                    <set>
                        <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                            <ref bean="{concat('weatherTsysSelector', '_', @name)}" />
                        </xsl:if>
                    </set>
                </property>
                <property name="partialUpdates">
                    <set>
                        <ref bean="weatherUpdater"/>
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
                <bean id="{concat('sciRanker', '_', @name)}" class="alma.scheduling.algorithm.sbranking.ScienceGradeRanker" scope="prototype">
                    <constructor-arg><value>sciRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                <xsl:variable name="weight" select="Scorers/HourAngleScorer/weight"/>
                <bean id="{concat('hourAngleRanker', '_', @name)}" class="alma.scheduling.algorithm.obsproject.HourAngleRanker" scope="prototype">
                    <constructor-arg><value>hourAngleRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <bean id="{concat('finalRanker', '_', @name)}" class="alma.scheduling.algorithm.sbranking.FinalRanker" scope="prototype">
                <constructor-arg><value>finalRanker</value></constructor-arg>
                <property name="rankers">
                    <set>
                        <xsl:if test="count(Scorers/SciScorer) = 1">
                            <ref bean="{concat('sciRanker', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                            <ref bean="{concat('hourAngleRanker', '_', @name)}"/>
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
    
    <xsl:template match="/SchedulingPolicy">
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
            
            
             <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                 <bean id="{concat('weatherTsysSelector', '_', @name)}" class="alma.scheduling.algorithm.weather.WeatherTsysSelector"
                     scope="prototype">
                     <constructor-arg><value>weatherTsysSelector</value></constructor-arg>
                     <property name="schedBlockDao" ref="sbDao"/>
                     <property name="tsysVariation" value="0.2"/>
                 </bean>
             </xsl:if>
            
            <bean id="{concat('preUpdateSelector', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelector"
                scope="prototype">
                <property name="selectors">
                    <set>
                        <xsl:if test="count(SelectionCriterion/ExecutiveSelector) = 1">
                            <ref bean="execSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SourceSelector) = 1">
                            <ref bean="sourceSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/ArrayConfigSelector) = 1">
                            <ref bean="arrayConfigSelector"/>
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/HourAngleSelector) = 1">
                            <ref bean="hourAngleSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SunAvoidanceSelector) = 1">
                            <ref bean="sunAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/MoonAvoidanceSelector) = 1">
                            <ref bean="moonAvoidanceSelector" />
                        </xsl:if>
                        <xsl:if test="count(SelectionCriterion/SchedBlockGradeSelector) = 1">
                            <ref bean="sbStatusSelector" />
                        </xsl:if>
                    </set>
                </property>
            </bean>
            
            <bean id="{concat('postUpdateSelectorAndUpdater', '_', @name)}" class="alma.scheduling.algorithm.sbselection.MasterSelectorWithUpdater">
                <property name="selectors">
                    <set>
                        <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                            <ref bean="{concat('weatherTsysSelector', '_', @name)}" />
                        </xsl:if>
                    </set>
                </property>
                <property name="partialUpdates">
                    <set>
                        <ref bean="weatherUpdater"/>
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
                <bean id="{concat('sciRanker', '_', @name)}" class="alma.scheduling.algorithm.sbranking.ScienceGradeRanker" scope="prototype">
                    <constructor-arg><value>sciRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                <xsl:variable name="weight" select="Scorers/HourAngleScorer/weight"/>
                <bean id="{concat('hourAngleRanker', '_', @name)}" class="alma.scheduling.algorithm.obsproject.HourAngleRanker" scope="prototype">
                    <constructor-arg><value>hourAngleRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <bean id="finalRanker" class="alma.scheduling.algorithm.sbranking.FinalRanker" scope="prototype">
                <constructor-arg><value>finalRanker</value></constructor-arg>
                <property name="rankers">
                    <set>
                        <xsl:if test="count(Scorers/SciScorer) = 1">
                            <ref bean="{concat('sciRanker', '_', @name)}"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                            <ref bean="{concat('hourAngleRanker', '_', @name)}"/>
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
                <property name="ranker" ref="finalRanker"/>
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
            
        </beans>
        
    </xsl:template>
    
</xsl:stylesheet>
