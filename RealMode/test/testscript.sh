#! /bin/bash

echo " Starting hugoActivator"
acsStartContainer hugoActivator&
sleep 20

# Run test
echo " Running test"


abeansStart -endorsed alma.scheduling.test.SchedulingTestClient


maciActivatorShutdown hugoActivator >&  /dev/null

