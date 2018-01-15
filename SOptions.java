
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SOptions {
	private static Shell sourceShell = new Shell();
	
	/* 			SOptions is an alternative for JOptionPane in swings
	 * 
	 * NOTE : Please call init() method before calling any other methods to specify the source Shell object
	 * 		  or else It uses new Shell object which may lead to incorrect behavior of dialogs
	 * 
	 * showConfirm() and showQuestion() methods returns true only if 'OK' and 'YES' button is clicked on respective dialogs
	 * or else returns false
	 */
	
	
	// Initialize with a shell object as source for dialogs
	static void init(Shell srcShell) {
		sourceShell = srcShell;
	}
	
	// Shows error dialog with 'OK' Button
	static public void showError(String title, String message) {
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_ERROR | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
	
	// Show confirmation dialog with 'OK' and 'CANCEL' Buttons
	static public boolean showConfirm(String title, String message) {
		int result;
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText(title);
		dialog.setMessage(message);
		result = dialog.open();
		if(result == SWT.OK) {
			return true;
		}
		return false;
	}
	
	// Shows Confirmation dialog with 'YES' and 'NO' Buttons
	static public boolean showQuestion(String title, String message) {
		int result;
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		dialog.setText(title);
		dialog.setMessage(message);
		result = dialog.open();
		if(result == SWT.YES) {
			return true;
		}
		return false;
	}
	
	// Shows information dialog with 'OK' Button
	static public void showInformation(String title, String message) {
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_INFORMATION | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
	
	// Shows warning dialog with 'OK' Button
	static public void showWarning(String title, String message) {
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_WARNING | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
}