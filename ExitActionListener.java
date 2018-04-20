import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

public class ExitActionListener implements ActionListener{
	public final static String loggerName = "default.runtime";

	static Logger logger;
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(!VolatileBag.isExit) {
			logger = Logger.getLogger(loggerName);
			logger.info("Exit Menu Item Clicked");
			VolatileBag.isExit = true;
			boolean flag = SOptions.showConfirm(null, "PROSecurity - Exit?", "Are you sure to exit PRO-Security?");
			if(flag) {
				logger.info("Exit confirmed, Starting clean-up process");
				
				// TODO Clean up operations
				VolatileBag.serverThread.close();
				System.exit(0);
			}
			logger.info("Exit declined.");
			VolatileBag.isExit = false;
		}
	}
}
