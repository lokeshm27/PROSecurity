import java.io.IOException;
import java.util.regex.Pattern;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SafeViewer extends SelectionAdapter {
	Safe safe;

	Shell dialog, parent;
	GridLayout dialogLayout, optionsLayout, bluetoothLayout, passwordLayout;
	GridData gridData;

	Composite options;

	Group bluetooth, password;

	Text safeName, email, password1, password2, hint;

	Label sizeLabel, deviceLabel;

	Button button1, button2, button3, chooseButton, clearButton, passwordButton, bluetoothOption, passwordOption,
			bothOption;

	Scale sizeScale;

	RemoteDevice device;

	int[] sizeInt = { 128, 256, 512, 768, 1024, 1280, 1536, 1792, 2048 };
	String[] sizeString = { " 0.1 GB ", " 0.25 GB ", " 0.5 GB ", " 0.75 GB ", " 1 GB ", " 1.25 GB", " 1.5 GB ",
			" 1.75 GB ", " 2 GB " };

	boolean newSafeMode;

	/*
	 * Constructor to add new Safe
	 */
	public SafeViewer(Shell parent) {
		newSafeMode = true;
		this.parent = parent;
		init();

		dialog.setText("Add New Safe - PROSecurity");
		sizeLabel.setText(" 1 GB ");
		sizeScale.setSelection(5);

		bluetoothOption.setSelection(true);

		deviceLabel.setText("No Device Selected");
		chooseButton.setText(" Choose ");

		setEnabled(password, false);

		button1.setText(" Back ");
		button2.setText(" Clear ");
		button3.setText(" Add ");

		Rectangle screenSize = parent.getDisplay().getPrimaryMonitor().getBounds();
		dialog.setLocation((screenSize.width - dialog.getBounds().width) / 2,
				(screenSize.height - dialog.getBounds().height) / 2);

		dialog.open();
		/*
		 * Display display = parent.getDisplay(); while (!dialog.isDisposed()) {
		 * if(!display.readAndDispatch()) display.sleep(); }
		 */

	}

	/*
	 * Constructor to edit safe
	 */
	public SafeViewer(Shell parent, Safe safe) {
		this.parent = parent;
		this.safe = safe;
	}

	/*
	 * Initializes the basic frame contents
	 */
	private void init() {
		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		dialog.setSize(500, 410);

		dialogLayout = new GridLayout();
		dialogLayout.numColumns = 3;
		dialogLayout.marginWidth = 20;
		dialogLayout.marginHeight = 20;
		dialogLayout.verticalSpacing = 10;
		dialog.setLayout(dialogLayout);

		new Label(dialog, SWT.NONE).setText("Safe Name: ");

		safeName = new Text(dialog, SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		safeName.setLayoutData(gridData);

		new Label(dialog, SWT.NONE).setText("Size: ");

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = 50;
		sizeLabel = new Label(dialog, SWT.NONE);
		sizeLabel.setText("1 GB");
		sizeLabel.setLayoutData(gridData);

		sizeScale = new Scale(dialog, SWT.HORIZONTAL);
		sizeScale.setMaximum(9);
		sizeScale.setMinimum(1);
		sizeScale.setPageIncrement(3);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		sizeScale.setLayoutData(gridData);

		options = new Composite(dialog, SWT.NONE);
		options.setSize(460, 560);
		optionsLayout = new GridLayout();
		optionsLayout.numColumns = 4;
		options.setLayout(optionsLayout);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		options.setLayoutData(gridData);

		new Label(options, SWT.NONE).setText("Lock Using: ");
		bluetoothOption = new Button(options, SWT.RADIO);
		bluetoothOption.setText("Bluetooth Device");

		passwordOption = new Button(options, SWT.RADIO);
		passwordOption.setText("Password");

		bothOption = new Button(options, SWT.RADIO);
		bothOption.setText("Both");

		bluetooth = new Group(dialog, SWT.NONE);
		bluetooth.setText("Bluetooth");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 4;
		bluetooth.setLayoutData(gridData);

		bluetoothLayout = new GridLayout();
		bluetoothLayout.numColumns = 4;
		bluetooth.setSize(460, 560);
		bluetooth.setLayout(bluetoothLayout);

		new Label(bluetooth, SWT.NONE).setText("Bluetooth Device: ");

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		deviceLabel = new Label(bluetooth, SWT.NONE);
		deviceLabel.setLayoutData(gridData);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		chooseButton = new Button(bluetooth, SWT.PUSH);
		chooseButton.setLayoutData(gridData);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		clearButton = new Button(bluetooth, SWT.PUSH);
		clearButton.setText(" Clear ");
		clearButton.setLayoutData(gridData);

		Label recoveryLabel = new Label(bluetooth, SWT.NONE);
		recoveryLabel.setText("Recovery E-Mail: ");
		recoveryLabel.setToolTipText("Recovery E-Mail will be used in case the bluetooth device is lost");

		email = new Text(bluetooth, SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		email.setLayoutData(gridData);
		recoveryLabel.setToolTipText("Recovery E-Mail will be used in case the bluetooth device is lost");

		password = new Group(dialog, SWT.NONE);
		password.setText("Password");
		passwordLayout = new GridLayout();
		passwordLayout.numColumns = 2;
		password.setLayout(passwordLayout);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		password.setLayoutData(gridData);

		new Label(password, SWT.NONE).setText("Password: ");
		password1 = new Text(password, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		password1.setLayoutData(gridData);

		new Label(password, SWT.NONE).setText("Confirm Password: ");
		password2 = new Text(password, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		password2.setLayoutData(gridData);

		Label hintLabel = new Label(password, SWT.NONE);
		hintLabel.setText("Password Hint: ");
		hintLabel.setToolTipText("Password hint will help you to remember password incase you forget your password");

		hint = new Text(password, SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		hint.setLayoutData(gridData);
		hint.setToolTipText("Password hint will help you to remember password incase you forget your password");

		button1 = new Button(dialog, SWT.PUSH);

		button2 = new Button(dialog, SWT.PUSH);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		button3 = new Button(dialog, SWT.PUSH);
		button3.setLayoutData(gridData);

		dialog.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (SOptions.showConfirm(dialog, "Confirm - PROSecurity",
						"Are you sure to go back and discard all changes?")) {
					event.doit = true;
				} else {
					event.doit = false;
				}
			}
		});

		chooseButton.addSelectionListener(chooseAdapter);
		clearButton.addSelectionListener(clearAdapter);
		sizeScale.addSelectionListener(sizeScaleAdapted);
		bluetoothOption.addSelectionListener(bluetoothAdapter);
		passwordOption.addSelectionListener(passwordAdapter);
		bothOption.addSelectionListener(bothAdapter);
		button1.addSelectionListener(button1Adapter);
		button2.addSelectionListener(button2Adapter);
		button3.addSelectionListener(button3Adapter);
		
	}

	/*
	 * Disables/enables all children of the composite recursively
	 * 
	 * @param Control: parent composite or group
	 * 
	 * @param enbled: boolean
	 */
	private void setEnabled(Control ctrl, boolean enabled) {
		if (ctrl instanceof Composite) {
			Composite comp = (Composite) ctrl;
			for (Control c : comp.getChildren())
				setEnabled(c, enabled);
			comp.setEnabled(enabled);
		} else {
			ctrl.setEnabled(enabled);
		}
	}

	/*
	 * SelectionAdapter for choose button
	 */
	private SelectionAdapter chooseAdapter = new SelectionAdapter() {
		Shell shell;
		boolean busy;
		Link link1, link2;
		List list;
		RemoteDevice[] deviceList;
		Button selectButton;

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			busy = true;

			shell = new Shell(dialog, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
			shell.setText("Choose a Device - PROSecurity");
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.marginHeight = 20;
			layout.marginWidth = 20;
			shell.setLayout(layout);

			link1 = new Link(shell, SWT.NONE);
			link1.setText("<a>Loading list...</a>");
			link1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg1) {
					if (!busy) {
						loadList();
					}
				}
			});

			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
			gridData.grabExcessHorizontalSpace = true;
			link1.setLayoutData(gridData);

			gridData = new GridData();
			gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
			gridData.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
			gridData.widthHint = 250;
			gridData.heightHint = 200;
			list = new List(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			list.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectButton.setEnabled(true);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					selectAction();
				}
			});
			list.setLayoutData(gridData);

			link2 = new Link(shell, SWT.NONE);
			link2.setText("<a>Can't find your device?</a>");
			link2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg1) {
					SOptions.showInformation(shell, "Help - PROSecurity",
							"PROSecurity only shows devices which are paired with this computer and supports bluetooth version 3.0 or higher."
									+ "\nPlease add your bluetooth device in the settings and then try again.");
				}
			});

			gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
			selectButton = new Button(shell, SWT.PUSH);
			selectButton.setText(" Select ");
			selectButton.setLayoutData(gridData);
			selectButton.setEnabled(false);
			selectButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectAction();
				}
			});

			shell.pack();
			Rectangle screenSize = parent.getDisplay().getPrimaryMonitor().getBounds();
			shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
					(screenSize.height - shell.getBounds().height) / 2);
			shell.open();
			loadList();
		}

		private void selectAction() {
			int i = list.getSelectionIndex();
			if (i != -1) {
				try {
					deviceLabel.setText(deviceList[i].getFriendlyName(false));
					device = deviceList[i];
					chooseButton.setText("Change");
					shell.dispose();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				SOptions.showError(shell, "Error - PROSecurity", "Please select an option.!");
			}
		}

		private void loadList() {
			boolean firstTime = true;
			System.out.println("Loading list..");
			boolean retry = true;
			if (!link1.getText().equals("<a>Loading list...</a>")) {
				link1.setText("<a>Refreshing...</a>");
				busy = true;
				firstTime = false;
			}

			while (retry && !LocalDevice.isPowerOn()) {
				retry = SOptions.showConfirm(shell, "Bluetooth Search failed - PROSecurity",
						"Failed to search for Bluetooth devices as Bluetooth is turned off. Please turn On the Bluetooth and Click 'OK' to retry.");
			}

			if (!LocalDevice.isPowerOn()) {
				shell.dispose();
				return;
			}

			try {
				deviceList = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
				if (deviceList.length == 0) {
					SOptions.showError(shell, "Empty - PROSecurity",
							"No Paired bluetooth devices found.! Please try again.");
				} else {
					list.removeAll();
					for (RemoteDevice device : deviceList) {
						String temp = "Name: " + device.getFriendlyName(false) + "    MAC: "
								+ device.getBluetoothAddress();
						list.add(temp);
					}
				}
			} catch (BluetoothStateException e) {
				// TODO
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				busy = false;
				if (!firstTime)
					SOptions.showInformation(shell, "Success - PROSecurity",
							"Device list has been successfully updated.!");
				link1.setText("<a>Refresh</a>");
				selectButton.setEnabled(false);
			}
		}
	};

	/*
	 * 
	 * SelectionAdapter for clear button
	 */
	private SelectionAdapter clearAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			deviceLabel.setText("No Device Selected");
			chooseButton.setText("Choose");
			device = null;
		}
	};

	/*
	 * Selection adapter for size Scale
	 */
	private SelectionAdapter sizeScaleAdapted = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			sizeLabel.setText(sizeString[sizeScale.getSelection() - 1]);
		}
	};

	/*
	 * Selection adapter for Bluetooth option
	 */
	private SelectionAdapter bluetoothAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			password1.setText("");
			password2.setText("");
			hint.setText("");
			setEnabled(password, false);

			if (!bluetooth.isEnabled()) {
				clearAdapter.widgetSelected(null);
				email.setText("");
				setEnabled(bluetooth, true);
			}
		}
	};

	/*
	 * Selection adapter for password option
	 */
	private SelectionAdapter passwordAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			clearAdapter.widgetSelected(null);
			setEnabled(bluetooth, false);
			email.setText("");

			if (!password.isEnabled()) {
				setEnabled(password, true);
				password1.setText("");
				password2.setText("");
				hint.setText("");
			}
		}
	};

	/*
	 * Selection adapter for both option
	 */
	private SelectionAdapter bothAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (!bluetooth.isEnabled()) {
				clearAdapter.widgetSelected(null);
				email.setText("");
				setEnabled(bluetooth, true);
			}

			if (!password.isEnabled()) {
				setEnabled(password, true);
				password1.setText("");
				password2.setText("");
				hint.setText("");
			}
		}
	};

	/*
	 * Selection adapter for back/cancel button
	 */
	private SelectionAdapter button1Adapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (SOptions.showConfirm(dialog, "Confirm - PROSecurity",
					"Are you sure to go back and discard all changes?")) {
				dialog.dispose();
			}
		}
	};

	/*
	 * Selection adapter for clear/delete button
	 */
	private SelectionAdapter button2Adapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (newSafeMode) {
				if (SOptions.showConfirm(dialog, "Confirm - PROSecurity",
						"Are sure to clear all fields to default?\nChanges will not be saved.")) {
					safeName.setText("");
					sizeLabel.setText(sizeString[4]);
					sizeScale.setSelection(5);
					clearAdapter.widgetSelected(null);
					email.setText("");
					
					bluetoothOption.setSelection(true);
					setEnabled(bluetooth, true);
					password1.setText("");
					password2.setText("");
					hint.setText("");
					setEnabled(password, false);
				}
			} else {
				// TODO
			}
		}
	};
	
	
	/*
	 * Selection adapter for add/Update button
	 */
	private SelectionAdapter button3Adapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if(!validateData())
				return;
			
			if(newSafeMode) {
				
			} else {
				// TODO
			}
			
			
		}
		
		boolean validateData() {
			// Validate data
			
			// Validating name
			if(safeName.getText().isEmpty()) {
				SOptions.showError(dialog, "Error - PROSecurity", "Safe name can not be empty.!\nPlease enter a valid safe name.");
				return false;
			}
			String nameString = safeName.getText();
			if(nameString.length() > 10) {
				SOptions.showError(dialog, "Error - PROSecurity", "Safe name can not be more than 10 characters.!\nPlease enter a valid safe name.");
				return false;
			}
			
			if(!nameString.matches("[-_a-zA-Z0-9 ]*")) {
				SOptions.showError(dialog, "Error - PROSecurity", "Safe name can not contain any special characters except: \'-\' and \'_\'"
						+ " \nPlease enter a valid safe name.");
				return false;
			}
			
			if(bluetoothOption.getSelection()) {
				if(!validateBluetooth())
					return false;
			}
			
			if(passwordOption.getSelection()) {
				if(!validatePassword())
					return false;
			}
			
			if(bothOption.getSelection()) {
				if(!validateBluetooth())
					return false;
				
				if(!validatePassword())
					return false;
			}
			
			SOptions.showInformation(dialog, "Success - PROSecurity", "Data has been validated.!");
			return true;
		}
		
		boolean validateBluetooth() {
			if(device == null) {
				SOptions.showError(dialog, "Error - PROSecurity", "Please choose a bluetooth device.!");
				return false;
			}
			
			if(email.getText().isEmpty()) {
				SOptions.showError(dialog, "Error - PROSecurity", "E-Mail can not be empty.!"
						+ "\nPlease enter a valid E-Mail Id");
				return false;
			}
			
			Pattern ptr = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
			if(!ptr.matcher(email.getText()).find()) {
 				SOptions.showError(dialog, "Error - PROSecurity", "Invalid E-Mail Id.!"
						+ "\nPlease enter a valid E-Mail Id");
				return false;
 			}
			
			return true;
		}
		
		boolean validatePassword() {
			String pwd1 = password1.getText(),pwd2 = password2.getText(),hintString = hint.getText();
			if(pwd1.isEmpty()) {
				SOptions.showError(dialog, "Error - PROSecurity", "Password can not be empty.!"
						+ "\nPlease enter a password");
				return false;
			}
			
			if(pwd2.isEmpty()) {
				SOptions.showError(dialog, "Error - PROSecurity", "Password can not be empty.!"
						+ "\nPlease enter a password");
				return false;
			}
			
			if(pwd1.length() < 4) {
				SOptions.showError(dialog, "Error - PROSecurity", "Password should be atleast 4 characters."
						+ "\nPlease enter a valid password");
				return false;
			}
			
			if(!pwd1.equals(pwd2)) {
				SOptions.showError(dialog, "Error - PROSecurity", "Passwords does not match.!"
						+ "\nPlease check the passwords");
				return false;
			}
			
			if(hintString.isEmpty()) {
				if(!SOptions.showConfirm(dialog, "Continue without password hint? - PROSecurity", "Password hint will help you to remember password in case your forgot it."
						+ "\nAre you sure to continue without password hint?")) {
					return false;
				}
			}
			
			return true;
		}
	};

}