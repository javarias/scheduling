package alma.scheduling;

import alma.scheduling.planning_mode_sim.cli.AprcTool;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class DSAUno extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              alma.scheduling.XDSAUno
{
    private final XComponentContext m_xContext;
    private static final String m_implementationName = DSAUno.class.getName();
    private static final String[] m_serviceNames = {
        "alma.scheduling.DSAUnoService" };


    public DSAUno( XComponentContext context )
    {
        m_xContext = context;
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(DSAUno.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // alma.scheduling.XDSAUno:
    public void load()
    {
      //  AprcTool tool = new AprcTool();
      //  tool.hello();
        try {
            AprcTool tool = new AprcTool();
            String[] args = {"load"};
            tool.selectAction(args);
        } catch (IOException ex) {
            Logger.getLogger(DSAUno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run()
    {
        try {
            AprcTool tool = new AprcTool();
            String[] args = {"run"};
            tool.selectAction(args);
        } catch (IOException ex) {
            Logger.getLogger(DSAUno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
