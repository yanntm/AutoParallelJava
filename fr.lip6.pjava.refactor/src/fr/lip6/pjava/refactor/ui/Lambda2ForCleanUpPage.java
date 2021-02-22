package fr.lip6.pjava.refactor.ui;

import org.eclipse.jdt.core.manipulation.CleanUpOptionsCore;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpConfigurationUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class Lambda2ForCleanUpPage implements ICleanUpConfigurationUI {
	 private CleanUpOptions fOptions;
	@Override
	public void setOptions(CleanUpOptions options) {
		this.fOptions = options;
		
	}

	@Override
	public Composite createContents(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		//final Group g = new Group(parent, SWT.NONE);
		//final Label l = new Label(g, SWT.NONE);
		final Button b = new Button(c, SWT.CHECK);
		b.setText("Activer la transformation des boucles for");
		if(fOptions.isEnabled("cleanup.transform_enhanced_for")) b.setSelection(true);
		b.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(b.getSelection()) {
					fOptions.setOption("cleanup.transform_enhanced_for", CleanUpOptionsCore.TRUE);
				}else {
					fOptions.setOption("cleanup.transform_enhanced_for", CleanUpOptionsCore.FALSE);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return c;
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
