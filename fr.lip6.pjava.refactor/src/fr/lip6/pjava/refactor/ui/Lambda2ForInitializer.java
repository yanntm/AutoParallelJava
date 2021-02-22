package fr.lip6.pjava.refactor.ui;

import org.eclipse.jdt.core.manipulation.CleanUpOptionsCore;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpConfigurationUI;
import org.eclipse.jdt.ui.cleanup.ICleanUpOptionsInitializer;
import org.eclipse.swt.widgets.Composite;

public class Lambda2ForInitializer implements ICleanUpOptionsInitializer  {

	@Override
	public void setDefaultOptions(CleanUpOptions options) {
		// TODO Auto-generated method stub
		options.setOption("cleanup.transform_enhanced_for", CleanUpOptionsCore.TRUE);
	}


}
