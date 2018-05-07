import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class LockActionListener implements ActionListener {

	public final static String loggerName = "default.runtime";
	public final static String resPath = StartingPoint.resPath;
	String mac;
	Logger logger;
	Display display;
	Shell shell, messageBox;
	Button enableButton;
	Group bluetooth, options;
	Label deviceLabel, lockLabel;
	Button chooseButton, clearButton;
	Button winOption, proOption;
	Button saveButton, cancelButton;
	Boolean working = false, winSelected = false;

	@Override
	public void actionPerformed(ActionEvent e) {
		logger = Logger.getLogger(loggerName);
		display = new Display();
		shell = new Shell(display, SWT.TITLE | SWT.CLOSE | SWT.BORDER | SWT.MIN);
		shell.setSize(450, 270);
		shell.setText("Smart Lock - PROSecurity");
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!working) {
					if (SOptions.showConfirm(shell, "Confirm - PROSecurity",
							"Are you sure to go back and discard any changes?")) {
						event.doit = true;
					} else {
						event.doit = false;
					}
				} else {
					event.doit = false;
				}
			}
		});

		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 3;
		parentLayout.marginWidth = 20;
		parentLayout.marginHeight = 20;
		parentLayout.verticalSpacing = 20;
		shell.setLayout(parentLayout);

		enableButton = new Button(shell, SWT.CHECK);
		enableButton.setText("Enable Smart Lock");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 3;
		enableButton.setLayoutData(gridData);
		enableButton.addSelectionListener(enableAdapter);

		options = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		options.setLayoutData(gridData);

		GridLayout optionsLayout = new GridLayout();
		optionsLayout.numColumns = 4;
		options.setSize(430, 200);
		options.setLayout(optionsLayout);

		lockLabel = new Label(options, SWT.NONE);
		lockLabel.setText("Lock using: ");

		winOption = new Button(options, SWT.RADIO);
		winOption.setText("Windows lock screen");

		proOption = new Button(options, SWT.RADIO);
		proOption.setText("Custom lock screen");

		bluetooth = new Group(shell, SWT.NONE);
		bluetooth.setText(" Select Bluetooth device ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		bluetooth.setLayoutData(gridData);

		GridLayout bluetoothLayout = new GridLayout();
		bluetoothLayout.numColumns = 4;
		bluetooth.setSize(430, 200);
		bluetooth.setLayout(bluetoothLayout);

		new Label(bluetooth, SWT.NONE).setText("Bluetooth device: ");

		deviceLabel = new Label(bluetooth, SWT.NONE);
		deviceLabel.setText("No Device Selected");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		deviceLabel.setLayoutData(gridData);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		chooseButton = new Button(bluetooth, SWT.PUSH);
		chooseButton.setText(" Choose ");
		chooseButton.setLayoutData(gridData);
		chooseButton.addSelectionListener(chooseAdapter);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		clearButton = new Button(bluetooth, SWT.PUSH);
		clearButton.setText(" Clear ");
		clearButton.setLayoutData(gridData);
		clearButton.addSelectionListener(clearAdapter);

		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText(" Cancel ");
		cancelButton.addSelectionListener(cancelAdapter);

		saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText(" Save ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 2;
		saveButton.setLayoutData(gridData);
		saveButton.addSelectionListener(saveAdapter);

		loadData();

		Rectangle screenSize = display.getPrimaryMonitor().getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
				(screenSize.height - shell.getBounds().height) / 2);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Loads data from dat file and updates UI
	 */
	void loadData() {
		File datFile = new File(resPath + "\\login.dat");
		if (datFile.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datFile));
				LockData lockData = (LockData) ois.readObject();
				ois.close();

				enableButton.setSelection(true);
				if (lockData.getLockType() == LockData.WIN_LOCK) {
					winOption.setSelection(true);
				} else {
					proOption.setSelection(true);
				}
				mac = lockData.getMac();
				deviceLabel.setText(BTOperations.getRemoteDevice(lockData.getMac()).getFriendlyName(false));
				chooseButton.setText(" Change ");
			} catch (IOException e) {
				logger.warning("IOException caught while reading login dat");
			} catch (ClassNotFoundException e) {
				logger.warning("ClassNotFoundException caught while reading login dat");
			}
		} else {
			enableButton.setSelection(false);
			lockLabel.setEnabled(false);
			winOption.setEnabled(false);
			proOption.setEnabled(false);
			setEnabled(bluetooth, false);
		}
	}

	/**
	 * Enables or disables components recursively
	 * 
	 * @param ctrl
	 *            Parent control
	 * @param enabled
	 *            boolean enable or disable
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

	/**
	 * Saves data into the dat file
	 */
	private void saveData() {

		LockData lockData;
		long service = 0;
		long[] uuid = BTOperations.getUUIDs();
		int i = 0;
		while (i < uuid.length) {
			try {
				logger.info(i + 1 + ": Checking for Service: " + uuid[i]);
				int j = 0;
				while (j < 3) {
					if (!BTOperations.checkRange(mac, uuid[i]))
						break;
					j++;
					logger.info("Success time " + j + 1 + " Sleep time 2");
					Thread.sleep(500);
				}
				if (j == 3) {
					service = uuid[i];
					break;
				}
				i++;
			} catch (InterruptedException e) {
				logger.warning("newSafe thread was interrupted using sleep time 2");
			}
		}

		if (i == uuid.length) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					SOptions.showError(messageBox, "PROSecurity -Error",
							"Failed to configure selected bluetooth device. Make sure the device is turned on and is within the range.\n"
									+ "Press try again");
					logger.info("Configurationg failed. Didn't respond for any services.");
					working = false;
					saveButton.setEnabled(true);
					cancelButton.setEnabled(true);
					messageBox.dispose();
				}
			});
			return;
		}

		if (winSelected) {
			lockData = new LockData(LockData.WIN_LOCK, mac, service);
		} else {
			lockData = new LockData(LockData.PRO_LOCK, mac, service);
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resPath + "\\login.dat"));
			oos.writeObject(lockData);
			oos.close();

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					SOptions.showInformation(shell, "PROSecurity - Success", "Save operation complete");
					working = false;
					messageBox.dispose();
					shell.dispose();
				}
			});
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException caught while saving data");
		} catch (IOException e) {
			logger.warning("IOException caught while saving data");
		}
	}

	/**
	 * SelectionAdapter for choose button
	 */
	private SelectionAdapter chooseAdapter = new SelectionAdapter() {
		Shell dialog;
		boolean busy;
		Link link1, link2;
		List list;
		RemoteDevice[] deviceList;
		Button selectButton;

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			busy = true;

			dialog = new Shell(shell, SWT.PRIMARY_MODAL);
			dialog.setText("Choose a Device - PROSecurity");
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.marginHeight = 20;
			layout.marginWidth = 20;
			dialog.setLayout(layout);
			logger.info("Loading list of bluetooth devices");
			link1 = new Link(dialog, SWT.NONE);
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
			list = new List(dialog, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
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

			link2 = new Link(dialog, SWT.NONE);
			link2.setText("<a>Can't find your device?</a>");
			link2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg1) {
					SOptions.showInformation(dialog, "Help - PROSecurity",
							"PROSecurity only shows devices which are paired with this computer and supports bluetooth version 3.0 or higher."
									+ "\nPlease add your bluetooth device in the settings and then try again.");
				}
			});

			gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
			selectButton = new Button(dialog, SWT.PUSH);
			selectButton.setText(" Select ");
			selectButton.setLayoutData(gridData);
			selectButton.setEnabled(false);
			selectButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectAction();
				}
			});

			dialog.pack();
			Rectangle screenSize = display.getPrimaryMonitor().getBounds();
			dialog.setLocation((screenSize.width - dialog.getBounds().width) / 2,
					(screenSize.height - dialog.getBounds().height) / 2);
			dialog.open();
			loadList();

		}

		private void selectAction() {
			int i = list.getSelectionIndex();
			if (i != -1) {
				try {
					deviceLabel.setText(deviceList[i].getFriendlyName(false));
					mac = deviceList[i].getBluetoothAddress();
					chooseButton.setText("Change");
					dialog.dispose();
				} catch (IOException e) {
					logger.warning("Unable to fetch name of the device as IOException occured: " + e.getMessage());
				}
			} else {
				SOptions.showError(dialog, "Error - PROSecurity", "Please select an option.!");
			}
		}

		private void loadList() {
			boolean firstTime = true;
			System.out.println("Loading list..");
			if (!link1.getText().equals("<a>Loading list...</a>")) {
				link1.setText("<a>Refreshing...</a>");
				busy = true;
				firstTime = false;
			}

			if (!BTOperations.isPowerOn(true)) {
				dialog.dispose();
				return;
			}

			try {
				deviceList = BTOperations.getPairedDevices();
				if (deviceList.length == 0) {
					SOptions.showError(dialog, "Empty - PROSecurity",
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
				logger.warning("Bluetooth Stack Exception occured while loading list: " + e.getMessage());
			} catch (IOException e) {
				logger.warning("IOException occured while loading list: " + e.getMessage());
			} finally {
				busy = false;
				if (!firstTime)
					SOptions.showInformation(dialog, "Success - PROSecurity",
							"Device list has been successfully updated.!");
				link1.setText("<a>Refresh</a>");
				selectButton.setEnabled(false);
			}
		}
	};

	/**
	 * SelectionAdapter for clear button
	 */
	private SelectionAdapter clearAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			deviceLabel.setText("No Device Selected");
			chooseButton.setText("Choose");
			mac = null;
		}
	};

	/**
	 * SelectionAdapter for cancel button
	 */
	private SelectionAdapter cancelAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (SOptions.showConfirm(shell, "PROSecurity - Confirm",
					"Are you sure to cancel?\nChanges may not be saved")) {
				shell.dispose();
			}
		}
	};

	/**
	 * SelectionAdapter for SaveButton
	 */
	private SelectionAdapter saveAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			working = true;
			cancelButton.setEnabled(false);
			saveButton.setEnabled(false);

			if (enableButton.getSelection()) {
				if (mac == null || mac.isEmpty()) {
					SOptions.showError(shell, "PROSecurity - Error", "Please select a Bluetooth device");
					working = false;
					saveButton.setEnabled(true);
					cancelButton.setEnabled(true);
					return;
				}
				if(winOption.getSelection()) {
					winSelected = true;
				} else {
					winSelected = false;
				}
				messageBox = new Shell(shell, SWT.PRIMARY_MODAL | SWT.TITLE | SWT.BORDER);
				messageBox.setText("Saving data - PROSecurity");
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				layout.marginWidth = 20;
				layout.marginHeight = 20;
				messageBox.setLayout(layout);

				new Label(messageBox, SWT.NONE).setText("Please wait...\nConfiguring device. This may take few seconds."
						+ "\n\nMake sure that bluetooth device is turned on and is within the range.");
				Thread saveThread = new Thread(new Runnable() {

					@Override
					public void run() {
						saveData();
					}
				});
				messageBox.pack();
				Rectangle screenSize = display.getPrimaryMonitor().getBounds();
				messageBox.setLocation((screenSize.width - messageBox.getBounds().width) / 2,
						(screenSize.height - messageBox.getBounds().height) / 2);
				messageBox.open();
				saveThread.start();

				while (!messageBox.isDisposed() && saveThread.isAlive()) {
					if (!messageBox.getDisplay().readAndDispatch()) {
						messageBox.getDisplay().sleep();
					}
				}
				messageBox.dispose();

			} else {
				File datFile = new File(resPath + "\\login.dat");
				int i = 0;
				if (datFile.exists() && i < 200) {
					datFile.delete();
				}
				SOptions.showInformation(shell, "PROSecurity - Success", "Save operation complete");
				shell.dispose();
			}
			working = false;
		}
	};

	/**
	 * SelectAdapter for enable option
	 */
	private SelectionAdapter enableAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (enableButton.getSelection()) {
				lockLabel.setEnabled(true);
				winOption.setEnabled(true);
				proOption.setEnabled(true);
				winOption.setSelection(true);
				proOption.setSelection(false);
				setEnabled(bluetooth, true);
				deviceLabel.setText("No Device Selected");
				chooseButton.setText(" Choose ");
			} else {
				deviceLabel.setText("No Device Selected");
				chooseButton.setText(" Choose ");
				mac = null;
				lockLabel.setEnabled(false);
				winOption.setEnabled(false);
				proOption.setEnabled(false);
				setEnabled(bluetooth, false);
			}
		}
	};

}