printf "###############################################\n"
./scripts/testEnv start
./scripts/runObsProjectTest.sh; RETURN=$?
./scripts/runObsUnitTest.sh; let "RETURN&=$?"
./scripts/runFieldSourceTest.sh; let "RETURN&=$?"
./scripts/runExecutiveTest.sh; let "RETURN&=$?"
printf "###############################################\n"
./scripts/testEnv stop
rm -f *.gclog
exit "$RETURN"
