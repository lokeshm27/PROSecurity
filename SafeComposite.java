import java.io.File;
import java.io.IOException;
import java.security.UnrecoverableEntryException;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SafeComposite extends Composite{
	int[] sizeInt = { 128, 256, 512, 768, 1024, 1280, 1536, 1792, 2048 };
	String[] sizeString = { " 0.1 GB ", " 0.25 GB ", " 0.5 GB ", " 0.75 GB ", " 1 GB ", " 1.25 GB", " 1.5 GB ",
			" 1.75 GB ", " 2 GB " };
	Logger logger;
	String password = null;
	String resPath = StartingPoint.resPath;
	
	public SafeComposite(Safe safe, Composite parent) {
		super(parent, SWT.BORDER);
		logger = Logger.getLogger("default.runtime");
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
		
		Button delete = new Button(this, SWT.PUSH);
		delete.setText(" Delete Safe ");
		if(safe.isUnlocked() || !safe.isAuthorized()) {
			delete.setEnabled(false);
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
		
		unlock.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean lockMode = true;
				if(unlock.getText() == " Unlock ") 
					lockMode = false;
				
				unlock.setText("Processing");
				delete.setEnabled(false);
				unlock.setEnabled(false);
				System.out.println(unlock.getText());
				if(!lockMode) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							safe.unlock();
							parent.getDisplay().asyncExec(new Runnable() {
								public void run() {
									if(safe.isUnlocked()) {
										unlock.setText(" Lock ");
										unlock.setEnabled(true);
										SOptions.showInformation(parent.getShell(), "PROSecurity - Success", "Safe " + safe.getName() + " successfully unlocked.");
										VolatileBag.updateSafe();
									} else {
										unlock.setText(" Unlock ");
										unlock.setEnabled(true);
										delete.setEnabled(true);
										SOptions.showInformation(parent.getShell(), "PROSecurity - Failed", "Failed to unlock Safe " + safe.getName());
										VolatileBag.updateSafe();
									}
								}
							});
						}
					}).start();
					
				} else {
					new Thread(new Runnable() {
						@Override
						public void run() {
							safe.lock();
							parent.getDisplay().asyncExec(new Runnable() {
								public void run() {
									unlock.setText(" Unlock ");
									delete.setEnabled(true);
									unlock.setEnabled(true);
									SOptions.showInformation(parent.getShell(), "PROSecurity - Success", "Safe " + safe.getName() + " successfully locked.");
									VolatileBag.updateSafe();
								}
							});
						}
					}).start();
				}
			}
		
		});
		
		delete.addSelectionListener(new SelectionAdapter() {
			
			void onFail() {
				VolatileBag.safeOngoing = false;
				delete.setEnabled(true);
				unlock.setEnabled(true);
				VolatileBag.updateSafe();
			}
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(!SOptions.showQuestion(parent.getShell(), "PROSecurity - Confirm", "Are you sure to delete the safe: " + safe.getName())) {
					return;
				}
				
				VolatileBag.safeOngoing = true;
				logger.info("Safe delete initiated..");
				delete.setEnabled(false);
				unlock.setEnabled(false);
				int lockType = safe.getLockType();
				if(lockType == Safe.MAC_ONLY || lockType == Safe.TWO_FACT) {
					try {
						logger.info("Authenticating BT Device");
						if(!BTOperations.checkRange(safe.getMac(), safe.getService())) {
							SOptions.showError(parent.getShell(), "PROSecurity - Failed", "Failed to delete safe: " + safe.getName()
											+ "\nBluetooth authentication failed.!");
							onFail();
							return;
						}
						logger.fine("BT Authentication complete");
					} catch (IllegalAccessException e) {
						System.out.println("IllegalAcces to BT attributes at delete button, safe composite");
						e.printStackTrace();
					}
					
				}
				
				if (lockType == Safe.PWD_ONLY || lockType == Safe.TWO_FACT) {
					logger.info("Requesting password");
					
					Shell shell = new Shell(parent.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					Display display = shell.getDisplay();
					shell.setText("PROSecurity - Unlock " + safe.getName());
					GridLayout layout = new GridLayout();
					layout.numColumns = 2;
					layout.marginHeight = 20;
					layout.marginWidth = 20;
					shell.setLayout(layout);

					new Label(shell, SWT.NONE).setText("Enter password: ");
					Text passwordBox = new Text(shell, SWT.PASSWORD);

					SelectionAdapter submitAdapter = new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							if (passwordBox.getText().isEmpty()) {
								SOptions.showError(shell, "PROSecurity - Error",
										"Password can not be empty.! Please enter password.");
								return;
							}
							password = passwordBox.getText();
							shell.dispose();
						}
					};

					passwordBox.addKeyListener(new KeyAdapter() {
						public void keyPressed(KeyEvent e) {
							if (e.keyCode == SWT.CR) {
								submitAdapter.widgetSelected(null);
							}
						}
					});

					GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
					gridData.horizontalSpan = 2;
					Button submit = new Button(shell, SWT.PUSH);
					submit.setText(" Submit ");
					submit.setLayoutData(gridData);
					submit.addSelectionListener(submitAdapter);

					shell.addListener(SWT.Close, new Listener() {
						@Override
						public void handleEvent(Event event) {
							password = null;
						}
					});

					shell.pack();
					Rectangle screenSize = display.getPrimaryMonitor().getBounds();
					shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
							(screenSize.height - shell.getBounds().height) / 2);
					shell.open();

					// Wait for dispose of the shell
					while (!shell.isDisposed()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
					// display.dispose();

					if (password == null || password.isEmpty()) {
						logger.warning("Did not recieve password");
						return;
					}

					try {
						VolatileBag.keyStorage.getKey(safe.getName(), password);
					} catch (UnrecoverableEntryException e1) {
						SOptions.showError(getShell(), "PROSecurity - Failed", "Failed to delete safe: " + safe.getName());
						logger.warning("Unrecoverable exception caught: " + e1.getMessage());
						onFail();
						return;
					} catch (NullPointerException e1) {
						SOptions.showError(getShell(), "PROSecurity - Failed", "Failed to delete safe: " + safe.getName());
						logger.severe("NullPointerException caught: " + e1.getMessage());
						onFail();
						return;
					} catch (IOException e1) {
						SOptions.showError(getShell(), "PROSecurity - Failed", "Failed to delete safe: " + safe.getName());
						logger.severe("IOException caught: " + e1.getMessage());
						onFail();
						return;
					}
					
					// Updating list
					VolatileBag.safes.remove(safe.getName());
					File safeFile = new File(resPath + "\\" + safe.getSafeFileName() + ".prhd");
					int i=0;
					while(safeFile.exists() && i<200)
						safeFile.delete();
					if(safe.getLockType() != Safe.MAC_ONLY) {
						try {
							VolatileBag.keyStorage.deleteEntry(safe.getName(), KeyStorage.defaultPassword);
						} catch (NullPointerException | UnrecoverableEntryException | IOException e1) {
							logger.warning("Unable to delete entry: " + e1.getMessage());
						}
					} else {
						try {
							VolatileBag.keyStorage.deleteEntry(safe.getName(), password);
						} catch (NullPointerException | UnrecoverableEntryException | IOException e1) {
							logger.warning("Unable to delete entry: " + e1.getMessage());
						}
					}
					VolatileBag.safeOngoing = false;
					VolatileBag.updateSafe();
					SOptions.showInformation(getShell(), "PROSecurity - Success", "Safe deletion successful");
				}
			}
		});
		
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
