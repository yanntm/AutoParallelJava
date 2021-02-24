package fr.lip6.pjava.refactor.ui;

import org.eclipse.jdt.core.manipulation.CleanUpOptionsCore;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpOptionsInitializer;

/**
 * Class use to initialize our plug-in by setting it as active
 * @author Teillet & Capitanio
 *
 */
public class Lambda2ForInitializer implements ICleanUpOptionsInitializer  {
	
	
	@Override
	public void setDefaultOptions(CleanUpOptions options) {
		/**
		 * if we want our plug-in to be activate : CleanUpOptionsCore.TRUE
		 * else CleanUpOptionsCore.FALSE
		 */
		options.setOption("cleanup.transform_enhanced_for", CleanUpOptionsCore.TRUE);
	}


}
