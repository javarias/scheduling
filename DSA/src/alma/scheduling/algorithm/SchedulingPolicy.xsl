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
            
            <xsl:if test="count(SelectionCriterion/ExecutiveSelector) = 1">
                <bean id="execSelector" class="alma.scheduling.algorithm.executive.ExecutiveSelector"
                    scope="prototype">
                    <constructor-arg><value>execSelector</value></constructor-arg>
                    <property name="execDao" ref="execDao"/>
                    <property name="sbDao" ref="sbDao"/>
                </bean>
            </xsl:if>
            
             <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                 <bean id="weatherTsysSelector" class="alma.scheduling.algorithm.weather.WeatherTsysSelector"
                     scope="prototype">
                     <constructor-arg><value>weatherTsysSelector</value></constructor-arg>
                     <property name="schedBlockDao" ref="sbDao"/>
                     <property name="tsysVariation" value="0.2"/>
                 </bean>
             </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/SourceSelector) = 1">
                <bean id="sourceSelector" class="alma.scheduling.algorithm.obsproject.FieldSourceObservableSelector"
                    scope="prototype">
                    <constructor-arg><value>sourceSelector</value></constructor-arg>
                    <property name="configDao" ref="configDao"/>
                    <property name="schedBlockDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/ArrayConfigSelector) = 1">
                <bean id="arrayConfigSelector" class="alma.scheduling.algorithm.observatory.ArrayConfigurationSelector"
                    scope="prototype">
                    <constructor-arg><value>arrayConfigSelector</value></constructor-arg>
                    <property name="sbDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/HourAngleSelector) = 1">
                <bean id="hourAngleSelector" class="alma.scheduling.algorithm.obsproject.HourAngleSelector"
                    scope="prototype">
                    <constructor-arg><value>hourAngleSelector</value></constructor-arg>
                    <property name="sbDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/MoonAvoidanceSelector) = 1">
                <bean id="moonAvoidanceSelector" class="alma.scheduling.algorithm.obsproject.MoonAvoidanceSelector"
                    scope="prototype">
                    <constructor-arg><value>moonAvoidanceSelector</value></constructor-arg>
                    <property name="sbDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/SunAvoidanceSelector) = 1">
                <bean id="sunAvoidanceSelector" class="alma.scheduling.algorithm.obsproject.SunAvoidanceSelector"
                    scope="prototype">
                    <constructor-arg><value>sunAvoidanceSelector</value></constructor-arg>
                    <property name="sbDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <xsl:if test="count(SelectionCriterion/SchedBlockGradeSelector) = 1">
                <bean id="sbStatusSelector" class="alma.scheduling.algorithm.obsproject.SchedBlockStatusSelector"
                    scope="prototype">
                    <constructor-arg><value>sbStatusSelector</value></constructor-arg>
                    <property name="sbDao" ref="sbDao"/>		
                </bean>
            </xsl:if>
            
            <bean id="preUpdateSelector" class="alma.scheduling.algorithm.sbselection.MasterSelector"
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
            
            <bean id="postUpdateSelectorAndUpdater" class="alma.scheduling.algorithm.sbselection.MasterSelectorWithUpdater">
                <property name="selectors">
                    <set>
                        <xsl:if test="count(SelectionCriterion/WeatherSelector) = 1">
                            <ref bean="weatherTsysSelector" />
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
            
            <bean id="weatherSelector" class="alma.scheduling.algorithm.weather.WeatherFullSelector"
                scope="prototype">
                <constructor-arg><value>weatherSelector</value></constructor-arg>
                <property name="schedBlockDao" ref="sbDao"/>
            </bean>
            
            <bean id="weatherUpdater" class="alma.scheduling.algorithm.weather.MemoryWeatherUpdater">
                <property name="dao" ref="atmDao"/>
                <property name="selector" ref="weatherSelector"/>
                <property name="weatherDao" ref="weatherStationDao"/>
                <property name="configDao" ref="configDao"/>
                <property name="projTimeIncr" value="0.5"/>
            </bean>
            
            <bean id="sourceUpdater" class="alma.scheduling.algorithm.obsproject.FieldSourceObservabilityUpdater">
                <property name="configDao" ref="configDao"/>
                <property name="sourceDao" ref="sourceDao"/>
            </bean>
            
            <xsl:if test="count(Scorers/SciScorer) = 1">
                <xsl:variable name="weight" select="string(Scorers/SciScorer/weight)"/>
                <bean id="sciRanker" class="alma.scheduling.algorithm.sbranking.ScienceGradeRanker" scope="prototype">
                    <constructor-arg><value>sciRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                <xsl:variable name="weight" select="Scorers/HourAngleScorer/weight"/>
                <bean id="hourAngleRanker" class="alma.scheduling.algorithm.obsproject.HourAngleRanker" scope="prototype">
                    <constructor-arg><value>hourAngleRanker</value></constructor-arg>
                    <property name="factor" value="{$weight}"/>
                </bean>
            </xsl:if>
            
            <bean id="finalRanker" class="alma.scheduling.algorithm.sbranking.FinalRanker" scope="prototype">
                <constructor-arg><value>finalRanker</value></constructor-arg>
                <property name="rankers">
                    <set>
                        <xsl:if test="count(Scorers/SciScorer) = 1">
                            <ref bean="sciRanker"/>
                        </xsl:if>
                        <xsl:if test="count(Scorers/HourAngleScorer) = 1">
                            <ref bean="hourAngleRanker"/>
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
                        <ref bean="preUpdateSelector"/>
                    </set>
                </property>
                <property name="postUpdateSelectors">
                    <set>
                        <ref bean="postUpdateSelectorAndUpdater"/>
                    </set>
                </property>
                
                <property name="firstRunUpdaters">
                    <set>
                        <ref bean="sourceUpdater"/>
                    </set>
                </property>
                <property name="updaters">
                    <set>
                        <ref bean="postUpdateSelectorAndUpdater"/>
                    </set>
                </property>
            </bean>
            
        </beans>
        
    </xsl:template>
    
</xsl:stylesheet>
