package alma.scheduling.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;



public class DbUtils {
    /** Password for connecting to the HSQLDB server */
    public static final String HSQLDB_PASSWORD = "";

    /** Username for connecting to the HSQLDB server */
    public static final String HSQLDB_USER = "sa";

    /** Basic URL for an HSQLDB file-based database */
    public static final String HSQLDB_FILE_URL = "jdbc:hsqldb:file:";

    /** Basic URL for an HSQLDB in-memory database */
    public static final String HSQLDB_MEMORY_URL = "jdbc:hsqldb:mem:ignored";
    
    /** Basic URL for an HSQLDB in-memory database */
    public static final String HSQLDB_SERVER_URL = "jdbc:hsqldb:hsql://localhost:8090";
    
    /** JDBC driver for HSQLDB */
    public static final String HSQLDB_JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    
    /** HSQLDB DDL script. It should be in SCHEDULING_JAR_FILE. */
    public static final String HSQLDB_CREATE_SQL_SCRIPT = 
    	"alma/scheduling/config/create_tables.sql";
    
    /** HSQLDB DB cleaning script. It should be in SCHEDULING_JAR_FILE. */
    public static final String HSQLDB_DELETE_SQL_SCRIPT = 
    	"alma/scheduling/config/drop_tables.sql";
    
    /**Jar library file */
    public static final String JAR_FILE = "SCHEDULING_DSA.jar";
    
    public static void createTables() throws Exception {
        Class.forName(HSQLDB_JDBC_DRIVER);
        String ddl = readFileFromJar(JAR_FILE, HSQLDB_CREATE_SQL_SCRIPT);
        Connection conn = DriverManager.getConnection(HSQLDB_SERVER_URL, 
                HSQLDB_USER, 
                HSQLDB_PASSWORD);
        runScript(ddl, conn);
        conn.close();
    }
    
    public static void dropTables() throws Exception {
        String ddl = readFileFromJar(JAR_FILE, HSQLDB_DELETE_SQL_SCRIPT);
        Connection conn = DriverManager.getConnection(HSQLDB_MEMORY_URL, 
                HSQLDB_USER, 
                HSQLDB_PASSWORD);
        runScript(ddl, conn);
        conn.close();
    }
    
    /**
     * Extracts a text file from a Jar library. Returns its contents as a string.
     * 
     * @param jar Jar library
     * @param file File to read from the Jar file
     * @return file contents
     * @throws IOException
     */
    protected static String readFileFromJar(String jar, String file)
        throws IOException {
        FileInputStream in = new FileInputStream(findAcsLibrary(jar));
        JarInputStream jarin = new JarInputStream(in);
        ZipEntry ze = jarin.getNextEntry();
        while (ze != null) {
            if (ze.getName().equals(file))
                break;
            ze = jarin.getNextEntry();
        }
        InputStreamReader converter = new InputStreamReader(jarin);
        BufferedReader reader = new BufferedReader(converter);

        StringBuffer ddlbuff = new StringBuffer();
        String line = reader.readLine();
        while (line != null) {
            ddlbuff.append(line + "\n");
            line = reader.readLine();
        }
        reader.close();
        return new String(ddlbuff);
    }
    
    /**
     * Searches for a library file in ACS library locations, first in ACSROOT
     * and second in INTROOT.
     * @param lib Library name
     * @return File path to the library, null if it is not in ACS library locations
     */
    protected static String findAcsLibrary(String lib) {
        String[] acsDirs = new String[] {"ACSROOT", "INTROOT"};
        for (String d : acsDirs) {
            String dir = System.getenv(d);
            if (dir != null) {
                String jar = dir + "/lib/" + lib;
                File f = new File(jar);
                if (f.exists()) return jar;
            }            
        }
        return null;
    }
    
    /**
     * Searches for a library file in ACS config locations, first in ACSROOT
     * and second in INTROOT.
     * @param file Configuration file name
     * @return File path to the configuration file, null if it is not in
     * ACS configuration locations
     */
    protected static String findAcsConfigFile(String file) {
        String[] acsDirs = new String[] {"ACSROOT", "INTROOT"};
        for (String d : acsDirs) {
            String dir = System.getenv(d);
            if (dir != null) {
                String cfgf = dir + "/config/" + file;
                File f = new File(cfgf);
                if (f.exists()) return cfgf;
            }            
        }
        return null;        
    }
    
    /**
     * Execute an SQL script.
     * @param script  The SQL script, as a single string
     * @param conn    Connection to the DB server
     * @throws SQLException
     */
    protected static void runScript( String script, Connection conn )
            throws SQLException {
    
        Statement stmt = conn.createStatement();
        String[] statements = script.split( ";", -1 );
        for( int i = 0; i < statements.length; i++ ) {
            String statement = statements[i].trim();
            if( statement.length() == 0 ) {
                // skip empty lines
                continue;
            }
            stmt.execute( statement );
        }
    }
}
