printf "###############################################\n"
./scripts/testEnv start
./scripts/runExecutiveRankerTest.sh; let "RETURN&=$?"
printf "###############################################\n"
./scripts/testEnv stop
rm -f *.gclog
exit "$RETURN"
