package liquibase.osgi;

import liquibase.servicelocator.CustomResolverServiceLocator;

public class OSGIServiceLocator extends CustomResolverServiceLocator {

	public OSGIServiceLocator() {
		super(new OSGIPackageScanClassResolver());
	}
}
