printf "###############################################\n"
./scripts/testEnv start
./runDsaTest.sh; RETURN=$?
# ./runOtherTest; let "RETURN&=$?"
printf "###############################################\n"
./scripts/testEnv stop
rm -f *.gclog
exit "$RETURN"
