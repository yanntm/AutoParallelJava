package fr.lip6.pjava.refactor.ui;

import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpConfigurationUI;
import org.eclipse.swt.widgets.Composite;

public class Lambda2ForInitializer implements ICleanUpConfigurationUI {
	 private CleanUpOptions fOptions;
	@Override
	public void setOptions(CleanUpOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Composite createContents(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCleanUpCount() {
		return 1;
	}

	@Override
	public int getSelectedCleanUpCount() {
		return fOptions.isEnabled("cleanup.transform_enhanced_for") ? 1 : 0; //$NON-NLS-1$
	}

	@Override
	public String getPreview() {
		StringBuffer buf = new StringBuffer();
		if(fOptions.isEnabled("cleanup.transform_enhanced_for")) {
			buf.append("List<Integer> l = new ArrayList<>();\n")
			   .append("stream(l).forEach( l -> System.out.println(l) );\n"); 
		}else {
			buf.append("List<Integer> l = new ArrayList<>();\n")
			   .append("for(Integer i : l){\n") 
			   .append("\tSystem.out.println(i);\n")
			   .append("}\n");
		}
		return buf.toString();

	}

}
