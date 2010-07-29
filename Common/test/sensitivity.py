#!/usr/bin/env python

import math

def pointSourceSensitivity(exposureTime, frequency, bandwidth, declination,
    numberAntennas, antennaDiameter, latitude, opacity, atmBrightnessTemperature):
    rho_e = antennaEfficiency(antennaDiameter, frequency)
    tsys = getTsys(declination, latitude, frequency, opacity, atmBrightnessTemperature)
    bandwidthHz = bandwidth * 1.0E9
    tmp = rho_e * tsys / ( correlatorEfficiency() * instrumentalDecorrelationCoeff() * atmosphericDecorrelationCoeff() )
    return tmp / math.sqrt( numberAntennas * (numberAntennas - 1) * bandwidthHz * exposureTime )
    

def antennaEfficiency(diameter, frequency):
    """
    Calculate antenna efficiency

    Parameters:
    diameter - antenna diameter (m)
    frequency - frequency (GHz) 
    Returns:
        Antenna efficiency    
    """
    boltzmann = 1.38E-16 * 1.0E23
    radius = diameter * 100.0 / 2.0;
    area = math.pi * radius * radius * illuminationEfficiency()
    ae = 2.0 * boltzmann / area
    appertureEff = appertureEfficiency(frequency)
    return ae / appertureEff

def appertureEfficiency(frequency):
    eps_zero = 0.80
    sigma = 25 # microns
    lmb = 2.998E5 / frequency
    arg = 4 * math.pi * sigma / lmb
    return eps_zero * ( math.e ** (-arg*arg) )

def illuminationEfficiency():
    return 0.8

def correlatorEfficiency():
    return 0.88

def instrumentalDecorrelationCoeff():
    return 1.0

def atmosphericDecorrelationCoeff():
    return 1.0

def getTsys(declination, latitude, frequency, opacity, atmBrightnessTemperature):
    latitudeRad = math.radians(latitude)
    decRad = math.radians(declination)
    sinDec = math.sin(decRad)
    sinLat = math.sin(latitudeRad)
    cosDec = math.cos(decRad)
    cosLat = math.cos(latitudeRad)
    sinAltitude = sinDec * sinLat + cosDec * cosLat
 
    airmass = 1.0 / sinAltitude

    Tamb = 270
    etaFeed = 0.95
    Trx = getReceiverTemperature(frequency)
    
    tauZero = opacity
    Tatm = atmBrightnessTemperature

    f = math.exp(tauZero * airmass)
    Tcmb = 2.725 # [K]
    
    Trx = planck(frequency, Trx)
    Tatm = planck(frequency, Tatm)
    Tamb = planck(frequency, Tamb)

    Tsys = Trx + Tatm * etaFeed * (1.0 - 1 / f) + Tamb * (1.0 - etaFeed)

    Tsys = f * Tsys + Tcmb
    return Tsys

def getReceiverTemperature(frequency):
    if frequency <= 31.3 and frequency <= 45.0:
        return 17.0
    elif frequency >= 67.0 and frequency <= 90.0:
        return 30.0
    elif frequency >= 84.0 and frequency <= 116.0:
        return 37.0
    elif frequency >= 125.0 and frequency <= 163.0:
        return 51.0
    elif frequency >= 163.0 and frequency <= 211.0:
        return 65.0
    elif frequency >= 211.0 and frequency <= 275.0:
        return 83.0
    elif frequency >= 275.0 and frequency <= 373.0:
        return 147.0
    elif frequency >= 385.0 and frequency <= 500.0:
        return 196.0
    elif frequency >= 602.0 and frequency <= 720.0:
        return 175.0
    elif frequency >= 787.0 and frequency <= 950.0:
        return 230.0
    else:
        return -1.0
   
def planck(frequency, temperature):
    k = 1.38E-23
    planck_const = 6.626e-34
    freqHz = frequency * 1.0E9
    tmp =  planck_const * freqHz / k
    return tmp / ( math.exp(tmp/temperature) - 1.0 )
