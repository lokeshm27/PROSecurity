import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class SafeComposite extends Composite{
	int[] sizeInt = { 128, 256, 512, 768, 1024, 1280, 1536, 1792, 2048 };
	String[] sizeString = { " 0.1 GB ", " 0.25 GB ", " 0.5 GB ", " 0.75 GB ", " 1 GB ", " 1.25 GB", " 1.5 GB ",
			" 1.75 GB ", " 2 GB " };

	public SafeComposite(Safe safe, Composite parent) {
		super(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing = 20;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		// layout.makeColumnsEqualWidth = true;
		setLayout(layout);
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText(" Name: " + safe.getName());
		FontData fontData = nameLabel.getFont().getFontData()[0];
		Font boldFont = new Font(parent.getDisplay(), new FontData(fontData.getName(), 14, SWT.BOLD));
		nameLabel.setFont(boldFont);
		
		int i=0;
		for(i=0; i<sizeInt.length; i++) {
			if(sizeInt[i] == safe.getSize())
				break;
		}
		new Label(this, SWT.NONE).setText(" Size: " + sizeString[i]);
		
		Button edit = new Button(this, SWT.PUSH);
		edit.setText(" Edit Safe ");
		if(safe.isUnlocked() || !safe.isAuthorized()) {
			edit.setEnabled(false);
		}
		
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = 2;
		gridData.heightHint = 40;
		gridData.widthHint = 70;
		Button unlock = new Button(this, SWT.NONE);
		unlock.setLayoutData(gridData);
		if(!safe.isUnlocked()) {
			unlock.setText(" Unlock ");
			if(!safe.isAuthorized()) {
				unlock.setEnabled(false);
			}
		} else {
			unlock.setText(" Lock ");
		}
		
		Label imageLabel = new Label(this, SWT.NONE);
		Image image;
		if(safe.getLockType() == Safe.MAC_ONLY) {
			image = new Image(null, this.getClass().getResourceAsStream("/images/bluetooth.png"));
			imageLabel.setToolTipText("Locked using Bluetooth");
		} else if(safe.getLockType() == Safe.TWO_FACT) {
			image = new Image(null, this.getClass().getResourceAsStream("/images/both.png"));
			imageLabel.setToolTipText("Locked using Bluetooth and Password");
		} else {
			image = new Image(null, this.getClass().getResourceAsStream("/images/key.png"));
			imageLabel.setToolTipText("Locked using Password");
		}
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		imageLabel.setImage(image);
		imageLabel.setLayoutData(gridData);
		
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gridData.horizontalSpan = 2;
		Link help = new Link(this, SWT.NONE);
		help.setLayoutData(gridData);
		help.setText("<a>Need help with this safe?</a>");
		help.setToolTipText("Use this option to recover this safe if you forgot password or lost your bluetooth device.");
	}

}
