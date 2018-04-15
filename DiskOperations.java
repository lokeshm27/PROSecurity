import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class DiskOperations {
	
	public static String rootPath = System.getenv("LocalAppData") + "\\PROSecurity";
	public static String resPath = rootPath + "\\Res";
	public static String tempPath = rootPath + "\\Temp";
	public final static String loggerName = "default.runtime";
	static Logger logger;
	
	/**
	 * Initializes logger
	 */
	public static void init() {
		logger = Logger.getLogger(loggerName);
		logger.info("DiskOperations Initialized");
	}
	
	/**
	 * Creates safe and attaches it to the disk
	 * @param safe SafeData object for which vhd file as to be created
	 */
	public static void createDisk(SafeData safe) {
		try {
			String scriptName = "createScript" + safe.getName() + ".txt";
			
			logger.info("Creating VHD for safe: " + safe.getName());
			PrintWriter pw = new PrintWriter(tempPath + "\\" + scriptName);
			pw.write(getCreateCommands(safe.getSafeFileName(), safe.getSize(), safe.getName(), getFreeLetter()));
			pw.close();
			
			runScript(scriptName);
			
			//Delete File
			new File(scriptName).delete();
			
			File sourceFile = new File(tempPath + "\\" + safe.getSafeFileName() + ".vhd");
			File destFile = new File(resPath + "\\" + safe.getSafeFileName() + ".prhd");
			Files.copy(sourceFile.toPath(), destFile.toPath());
			
			attachDisk(safe.getSafeFileName() + ".vhd");
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException caught while writing to script file: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("IOException caught while copying VHD File: " + e.getMessage());
		}
	}
	
	/**
	 * Attaches the specified disk
	 * @param safeName Name of the safe in .vhd format which is in Temp Folder
	 */
	public static void attachDisk(String safeName) {
		try {
			String scriptName = "attachScript" + safeName + ".txt";
			logger.info("attaching disk: " + safeName);
			
			PrintWriter pw = new PrintWriter(tempPath + "\\" + scriptName);
			pw.write("select vdisk file=\"" + tempPath + "\\" + safeName + "\n"
					+ "attach vdisk");
			pw.close();
			
			runScript(safeName);
			
			//Delete File
			new File(scriptName).delete();
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException caught while writing to script file: " + e.getMessage());
		}
	}
	
	/**
	 * Detaches the specified disk
	 * @param safeName Name of the safe in .vhd format which is in Temp Folder
	 */
	public static void dettachDisk(String safeName) {
		try {
			String scriptName = "detachScript" + safeName + ".txt";
			logger.info("detaching disk: " + safeName);
			
			PrintWriter pw = new PrintWriter(tempPath + "\\" + scriptName);
			pw.write("select vdisk file=\"" + tempPath + "\\" + safeName + "\n"
					+ "detach vdisk");
			pw.close();
			
			runScript(safeName);
			
			// Delete File
			new File(scriptName).delete();
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException caught while writing to script file: " + e.getMessage());
		}
	}
	
	/**
	 * Runs the diskpart script in background mode
	 * @param fileName Name of the script to be run in Temp Folder 
	 */
	private static void runScript(String fileName) {
		try {
			File parentDir = new File(tempPath);
			File diskLog = new File(tempPath + "\\diskpart.log");
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(parentDir);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(diskLog));
			pb.command("cmd", "/c", "Start", "/B", "diskpart", "/s", "script.txt");
			Process p = pb.start();
			p.waitFor();
			
			//Sleep for 15 secs recommended
			Thread.sleep(15000);
		} catch (IOException e) {
			logger.severe("IOException caught while running command: " + e.getMessage());
		} catch (InterruptedException e) {
			logger.severe("Interrupted while finishing execution: " + e.getMessage());
		}
	}
	
	/**
	 *  Returns the smallest drive letter which is greater than equal to P, and which is available
	 * @return Drive letter
	 */
	public static char getFreeLetter() {
		try {
			// Delete file
			new File(tempPath + "\\diskpart.log").delete();
			
			// Write script
			String scriptName = "letterCheckScript.txt";
			PrintWriter pw = new PrintWriter(tempPath + "\\" + scriptName);
			pw.write("list volume");
			pw.close();
						
			// Run the script
			runScript(scriptName);
			
			//Delete File
			new File(scriptName).delete();
			
			char biggestLetter = 'C';
			boolean ready = false;
			FileReader reader = new FileReader(tempPath + "\\log.txt");
			BufferedReader ip = new BufferedReader(reader);
			String line;
			while ((line = ip.readLine()) != null) {
				if (!line.isEmpty()) {
					if (!ready) {
						if (line.startsWith("  -")) {
							ready = true;
							continue;
						}
					} else {
						String word[] = line.split("[ ]+");
						if (word[3].length() == 1) {
							System.out.println("Letter: " + word[3]);
							if (biggestLetter < word[3].charAt(0)) {
								biggestLetter = word[3].charAt(0);
							}
						}
					}
				}
			}
			ip.close();
			biggestLetter = Character.toLowerCase(biggestLetter);
			System.out.println("Biggest Letter: " + biggestLetter);
			new File(tempPath + "\\diskpart.log").delete();
			if(biggestLetter >= 'p') {
				return biggestLetter++;
			}
			return 'p';
		} catch (Exception e) {
			logger.severe(e + "Exception caught while checking free drive letter: " + e.getMessage());
		}
		return 'p';
	}

	/** 
	 * Returns the string of command used to create the diskpart script
	 * @param name vhd file name
	 * @param size size of disk in MB
	 * @param label name to be set for the disk
	 * @param letter Letter to be assigned to the disk
	 * @return String containing commands that can be used to create the vhd of specified attributes
	 */
	private static String getCreateCommands(String name, int size, String label, char letter) {
		return "create vdisk file=\"" + tempPath + "\\" + name + ".vhd\" maximum=" + size + "\n"
				+ "select vdisk file=\"" + tempPath + "\\" + name + ".vhd\"\n"
				+ "attach vdisk\n"
				+ "convert mbr\n"
				+ "create partition primary\n"
				+ "format fs=ntfs label=\"" + label +"\" quick\n"
				+ "assign letter=" + letter + "\n"
				+ "detach vdisk";
	}

	/**
	 * Hides the given file by setting attributes:
	 * 		Hidden = true and System = true
	 * @param fileName Name of the file in Res folder
	 * @throws FileNotFoundException If file does not exist in Res folder
	 */
	public static void hideFile(String fileName) throws FileNotFoundException {
		String filePath = resPath + "\\" + fileName;
		logger.info("Hiding file: " + fileName);
		if(!(new File(filePath).exists())) {
			throw new FileNotFoundException("File not found: " + fileName);
		}
		try {
			Path file = Paths.get(filePath);
			Files.setAttribute(file, "dos:hidden", true);
			Files.setAttribute(file, "dos:system", true);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	
}
