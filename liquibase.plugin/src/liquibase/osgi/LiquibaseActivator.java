/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package liquibase.osgi;

import liquibase.servicelocator.ServiceLocator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LiquibaseActivator implements BundleActivator {
	
	public LiquibaseActivator() {
		
	}
	
    public void start(BundleContext bundleContext) throws Exception {    
    	ServiceLocator.setInstance(new OSGIServiceLocator());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        ServiceLocator.reset();
    }
}
