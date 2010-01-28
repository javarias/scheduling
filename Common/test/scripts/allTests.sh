printf "###############################################\n"
./scripts/testEnv start
./scripts/runObsProjectTest.sh; RETURN=$?
# ./scripts/runOtherTest; let "RETURN&=$?"
printf "###############################################\n"
./scripts/testEnv stop
rm -f *.gclog
exit "$RETURN"
