import random

class ArrayConfiguration:
    '''Configuration information for the simulation of an ALMA Control array'''

    def __init__(self, execFailureMode, execTimingMode, archFailureMode, archTimingMode):
        self.execFailureMode = execFailureMode
        self.execTimingMode = execTimingMode
        self.archFailureMode = archFailureMode
        self.archTimingMode = archTimingMode

    def setExecFailureMode(self, failureMode):
        self.execFailureMode = failureMode

    def setExecTimingMode(self, timingMode):
        self.execTimingMode = timingMode

    def setArchFailurMode(self, failureMode):
        self.archFailureMode = failureMode

    def setArchTimingMode(self, timingMode):
        self.archTimingMode = timingMode

    def getExecTime(self):
        return self.execTimingMode.time()

    def getArchTime(self):
        return self.archTimingMode.time()

    def execSucceeds(self):
        return not self.execFailureMode.fails()

    def archSucceeds(self):
        return not self.archFailureMode.fails()

    def __init__(self, arrayName):
        if (arrayName == "Array001"):
            self.execFailureMode = FailsNever()
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsNever()
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array002"):
            self.execFailureMode = FailsEvery(5)
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsNever()
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array003"):
            self.execFailureMode = FailsRandomly(0.25)
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsNever()
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array004"):
            self.execFailureMode = FailsNever()
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsEvery(5)
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array005"):
            self.execFailureMode = FailsNever()
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsRandomly(0.25)
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array006"):
            self.execFailureMode = FailsEvery(5)
            self.execTimingMode  = TimingNormal(10, 2)
            self.archFailureMode = FailsEvery(7)
            self.archTimingMode  = TimingConstant(5)
        elif (arrayName == "Array007"):
            self.execFailureMode = FailsEvery(5)
            self.execTimingMode  = TimingNormal(30, 10)
            self.archFailureMode = FailsEvery(7)
            self.archTimingMode  = TimingConstant(10)
        elif (arrayName == "Array008"):
            self.execFailureMode = FailsEvery(5)
            self.execTimingMode  = TimingNormal(30*60, 2*60)
            self.archFailureMode = FailsEvery(7)
            self.archTimingMode  = TimingNormal(60, 10)
        elif (arrayName == "Array009"):
            self.execFailureMode = FailsNever()
            self.execTimingMode  = TimingNormal(30*60, 2*60)
            self.archFailureMode = FailsNever()
            self.archTimingMode  = TimingNormal(60, 10)
        else:
            print("WARNING: Unrecognised array " + str(arrayName) + " using default, slow configuration")
            self.execFailureMode = FailsNever()
            self.execTimingMode  = TimingConstant(30*60)
            self.archFailureMode = FailsNever()
            self.archTimingMode  = TimingConstant( 2*60)

    def __str__(self):
        return "ArrayConfiguration(exec: " + str(self.execFailureMode) + ", " + str(self.execTimingMode) + ", arch: " + str(self.archFailureMode) + ", " + str(self.archTimingMode) + ")"


#
# Modelling of the failure of SBs
# ===============================
#
class FailureMode:
    '''Superclass for models of failure'''

    def fails(self):
        raise NotImplementedError("FailureMode subclass fails to implement fails() method and thus fails.")

    def __str__(self):
        return "FailureMode(supposed to be abstract!)"


class FailsNever(FailureMode):
    '''A failure mode of never failing'''

    def fails(self):
        return False

    def __str__(self):
        return "FailsNever()"


class FailsRandomly(FailureMode):
    '''A failure mode of failing at random'''

    def __init__(self, rate):
        self.rate = rate

    def fails(self):
        return random.random() < self.rate

    def __str__(self):
        return "FailsRandomly(" + str(self.rate) + ")"


class FailsEvery(FailureMode):
    '''A failure mode of failing every fixed number of goes'''

    def __init__(self, every):
        self.every = every
        self.goes  = 0

    def fails(self):
        self.goes = self.goes+1
        if self.goes == self.every:
            self.goes = 0
            return True
        else:
            return False

    def __str__(self):
        return "FailsEvery(" + str(self.every) + ")"


#
# Modelling of the timing of SBs
# ==============================
#
class TimingMode:
    '''Superclass for distributions of times'''

    def time(self):
        raise NotImplementedError("TimingMode subclass fails to implement time() method and thus fails this time.")

    def __str__(self):
        return "TimingMode(supposed to be abstract!)"


class TimingConstant(TimingMode):
    '''Always the same time'''

    def __init__(self, seconds):
        self.seconds = round(seconds)

    def time(self):
        return self.seconds

    def __str__(self):
        return "TimingConstant(" + str(self.seconds) + ")"


class TimingNormal(TimingMode):
    '''Normal distribution of times, but with -ve times not allowed'''

    def __init__(self, mean, variance):
        self.mean = mean
        self.variance = variance

    def time(self):
        seconds = -1
        while seconds <= 0:
            seconds = round(random.gauss(self.mean, self.variance))
        return seconds

    def __str__(self):
        return "TimingNormal(" + str(self.mean) + ", " + str(self.variance) + ")"



