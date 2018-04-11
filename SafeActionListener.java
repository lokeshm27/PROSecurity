import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SafeActionListener implements ActionListener{
	public final static String loggerName = "default.runtime";

	Logger logger;
	Shell shell;
	
	public SafeActionListener() {
		logger = Logger.getLogger(loggerName);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.info("Safe Menu Item clicked");
		showDialog();
	}
	
	
	public void showDialog() {
		Display display = new Display();
		shell = new Shell(display, SWT.TITLE | SWT.CLOSE | SWT.BORDER | SWT.MIN);
		shell.setSize(600, 500);
		shell.setText("Safes - PROSecurity");
		shell.setLayout(new FillLayout());
		
		Composite parent = new Composite(shell, SWT.BORDER);
		parent.setSize(500, 500);
		//GridData parentData = new GridData();
		//parentData.grabExcessHorizontalSpace = true;
		//parent.setLayoutData(parentData);
		GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 40;
		parentLayout.marginHeight = 20;
		parent.setLayout(parentLayout);
		
		// Label
		Label label = new Label(parent, SWT.NONE);
		label.setText("Safes: ");
		FontData fontData = label.getFont().getFontData()[0];
		Font font = new Font(display, new FontData(fontData.getName(), 18, SWT.BOLD));
		label.setFont(font);
		
		// Safe List
		buildList(parent);
		
		Button addButton = new Button(parent, SWT.PUSH);
		GridData addButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		addButton.setLayoutData(addButtonData);
		addButton.setText(" Add New ");
		addButton.addSelectionListener(addButtonListener);
		
		Rectangle screenSize = display.getPrimaryMonitor().getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2, (screenSize.height - shell.getBounds().height) / 2);
		shell.open();
		
		while (!shell.isDisposed()) {
		    if (!display.readAndDispatch())
		     {
		    	display.sleep();
		     }
		}
		display.dispose();
	}
	
	
	public void buildList(Composite parent) {
		Composite list = new Composite(parent, SWT.BORDER);
		list.setSize(400, 400);
		GridData listData = new GridData(GridData.FILL_BOTH);
		list.setLayoutData(listData);
		GridLayout listLayout = new GridLayout(1, false);
		list.setLayout(listLayout);
		Label label = new Label(list, SWT.NONE);
		label.setText("List goes here");
	}
	
	
	SelectionListener addButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			new SafeViewer(shell);
		}
		
	};


}
