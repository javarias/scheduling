package alma.scheduling.datamodel.executive.dao;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import alma.obops.dam.userreg.dao.PortalAccountDao;
import alma.obops.dam.userreg.domain.PortalAccount;
import alma.obops.dam.userreg.utils.UserRegistryBeanFactory;
import alma.scheduling.input.executive.generated.PI;
import alma.scheduling.input.executive.generated.PIMembership;

public class UserRegistryDaoImpl implements UserRegistryDao{

	private UserRegistryBeanFactory factory;
	
	public UserRegistryDaoImpl() {
		factory = new UserRegistryBeanFactory() {
		};
		factory.init("userRegistryRelationalContext.xml", Logger.getAnonymousLogger());
	}
	
	@Override
	public List<PI> getAllPI() {
		SortedSet<PortalAccount> accounts = ((PortalAccountDao)factory.getBean("portalAccountDao")).getAllAccounts();
		ArrayList<PI> retVal = new ArrayList<PI>(accounts.size());
		for (PortalAccount acc: accounts) {
			PI pi = new PI();
			pi.setEmail(acc.getId());
			pi.setName(acc.getFirstName() + " " + acc.getLastName());
			PIMembership pim[] = new PIMembership[1];
			pim[0] = new PIMembership();
			pim[0].setMembershipPercentage(100.0F);
			pim[0].setExecutiveRef(acc.getExecutive().toUpperCase());
			pi.setPIMembership(pim);
			retVal.add(pi);
		}
		return retVal;
	}

	
	public static void main(String[] args) throws IOException, MarshalException, ValidationException {
		UserRegistryDaoImpl dao = new UserRegistryDaoImpl();
		List<PI> pis = dao.getAllPI();
		Writer w = new FileWriter("ExecutiveList.xml");
		Marshaller marshaller = new Marshaller(w);
		marshaller.setSuppressNamespaces(true);
		marshaller.setSuppressXSIType(true);
		for(PI pi: pis) {
			marshaller.marshal(pi);
		}
		System.out.println(w.toString());
	}
}
