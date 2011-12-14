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


#
# Modelling of the failure of SBs
# ===============================
#
class FailureMode:
    '''Superclass for models of failure'''

    def fails(self):
        raise NotImplementedError("FailureMode subclass fails to implement fails() method and thus fails.")


class FailsNever(FailureMode):
    '''A failure mode of never failing'''

    def fails(self):
        return False


class FailsRandomly(FailureMode):
    '''A failure mode of failing at random'''

    def __init__(self, rate):
        self.rate = rate

    def fails(self):
        return random.random() < self.rate


class FailsRegularly(FailureMode):
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


#
# Modelling of the timing of SBs
# ==============================
#
class TimingMode:
    '''Superclass for distributions of times'''

    def time(self):
        raise NotImplementedError("TimingMode subclass fails to implement time() method and thus fails this time.")


class TimingConstant(TimingMode):
    '''Always the same time'''

    def __init__(self, seconds):
        self.seconds = round(seconds)

    def time(self):
        return self.seconds


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


