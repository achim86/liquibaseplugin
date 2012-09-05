/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package liquibase.osgi;

import java.io.IOException;
import java.net.URL;

import liquibase.servicelocator.DefaultPackageScanClassResolver;

import org.eclipse.core.runtime.FileLocator;

/**
 * Package scan resolver that works with OSGI frameworks (in theory all of them)
 */
public class OSGIPackageScanClassResolver extends DefaultPackageScanClassResolver {
    
	@Override
	protected URL customResourceLocator(final URL url) throws IOException {
		return FileLocator.toFileURL(url);
	}
}