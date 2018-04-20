
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SOptions {
	//private static Shell sourceShell = new Shell();
	
	/* 			SOptions is an alternative for JOptionPane in swings
	 * 
	 * showConfirm() and showQuestion() methods returns true only if 'OK' and 'YES' button is clicked on respective dialogs
	 * or else returns false
	 */
	
	
	// Shows error dialog with 'OK' Button
	static public void showError(Shell sourceShell, String title, String message) {
		if(sourceShell == null)
			sourceShell = new Shell(new Display());
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_ERROR | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
	
	
	// Show confirmation dialog with 'OK' and 'CANCEL' Buttons
	static public boolean showConfirm(Shell sourceShell, String title, String message) {
		if(sourceShell == null)
			sourceShell = new Shell(new Display());
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
	static public boolean showQuestion(Shell sourceShell, String title, String message) {
		if(sourceShell == null)
			sourceShell = new Shell(new Display());
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
	static public void showInformation(Shell sourceShell, String title, String message) {
		if(sourceShell == null)
			sourceShell = new Shell(new Display());
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_INFORMATION | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
	
	
	// Shows warning dialog with 'OK' Button
	static public void showWarning(Shell sourceShell, String title, String message) {
		if(sourceShell == null)
			sourceShell = new Shell(new Display());
		MessageBox dialog = new MessageBox(sourceShell, SWT.ICON_WARNING | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}


}