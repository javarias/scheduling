package alma.scheduling.datamodel.config;

public class ScienceGradeConfig {
    
    private static int totalPrj;
    
    /**
     * The number of A graded projects
     * 
     * Note: Projects with grade A + Projects with grade B +
     * Projects with grade C + Projects with grade D = number of total projects
     */
    private int nGradeAPrj;
    /**
     * The number of B graded projects
     * 
     * Note: Projects with grade A + Projects with grade B +
     * Projects with grade C + Projects with grade D = number of total projects
     */
    private int nGradeBPrj;
    /**
     * The number of C graded projects
     * 
     * Note: Projects with grade A + Projects with grade B +
     * Projects with grade C + Projects with grade D = number of total projects
     */
    private int nGradeCPrj;
    /**
     * The number of D graded projects. This number should be calculated
     * using the number of total projects and the rest of the projects with
     * grade A, B and C.
     * 
     * Note: Projects with grade A + Projects with grade B +
     * Projects with grade C + Projects with grade D = number of total projects
     */
    private int nGradeDPrj;
    
    
    public int getTotalPrj() {
        return ScienceGradeConfig.totalPrj;
    }

    public synchronized void setTotalPrj(int totalPrj){
        ScienceGradeConfig.totalPrj = totalPrj;
    }

    public int getnGradeAPrj() {
        return nGradeAPrj;
    }

    public void setnGradeAPrj(int nGradeAPrj) {
        this.nGradeAPrj = nGradeAPrj;
    }

    public int getnGradeBPrj() {
        return nGradeBPrj;
    }

    public void setnGradeBPrj(int nGradeBPrj){
        this.nGradeBPrj = nGradeBPrj;
    }

    public int getnGradeCPrj() {
        return nGradeCPrj;
    }

    public void setnGradeCPrj(int nGradeCPrj){
        this.nGradeCPrj = nGradeCPrj;
    }

    public int getnGradeDPrj() {
        return nGradeDPrj;
    }

    public void setnGradeDPrj(int nGradeDPrj){
        this.nGradeDPrj = nGradeDPrj;
    }

    /**
     * After set all the values the configuration should be tested by the user
     * 
     * @throws InvalidScienceGradeConfig if the configuration is invalid. That
     * means if the configuration contains negative values or if the sum of all
     * the number of grades is different to the total numbers of projects.
     */
    public void testValues() throws InvalidScienceGradeConfig {
        long res = nGradeAPrj + nGradeBPrj + nGradeCPrj + nGradeDPrj;
        if(res != totalPrj || totalPrj < 0 || nGradeAPrj < 0 || nGradeBPrj < 0 ||
                nGradeCPrj < 0 || nGradeDPrj < 0)
            throw new InvalidScienceGradeConfig();
    }
    
    public class InvalidScienceGradeConfig extends Exception{
        /**
         * 
         */
        private static final long serialVersionUID = -9020142181404428558L;
        
        @Override
        public String getMessage() {
            return "Invalid Science Grade configuration. Check the numbers used" +
            		" to configurate the max numbers of project by grade. (Total projects: " + totalPrj +
            		", projects grade nums: " + nGradeAPrj + ", " + nGradeBPrj + ", " + nGradeCPrj + ", " + nGradeDPrj + ", " +
            		" Sum: " + ( nGradeAPrj  + nGradeBPrj + nGradeCPrj  + nGradeDPrj ) + ")";
        }
    }
}
