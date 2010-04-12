printf "###############################################\n"
./scripts/testEnv start
./scripts/runObsUnitTest.sh; RETURN=$?
./scripts/runObsProjectTest.sh; let "RETURN&=$?"
./scripts/runFieldSourceTest.sh; let "RETURN&=$?"
./scripts/runExecutiveInputModelTest.sh; let "RETURN&=$?"
./scripts/runExecutiveTest.sh; let "RETURN&=$?"
./scripts/runExecutiveDaoTest.sh; let "RETURN&=$?"
./scripts/runOutputTest.sh; let "RETURN&=$?"
printf "###############################################\n"
./scripts/testEnv stop
rm -f *.gclog
exit "$RETURN"
