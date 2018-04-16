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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.filechooser.FileSystemView;

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
		String command = getCreateCommands(safe.getSafeFileName(), safe.getSize(), safe.getName(), getFreeLetter());
		logger.info("Creating VHD for safe: " + safe.getName());
		
		runCommand(command);
	}
	
	/**
	 * Attaches the specified disk
	 * @param safeName Name of the safe in .vhd format which is in Temp Folder
	 */
	public static void attachDisk(String safeName) {
		String command = "select vdisk file=\"" + tempPath + "\\" + safeName + "\"\n"
				+ "attach vdisk";
		logger.info("attaching disk: " + safeName);
		runCommand(command);
	}
	
	/**
	 * Detaches the specified disk
	 * @param safeName Name of the safe in .vhd format which is in Temp Folder
	 */
	public static void dettachDisk(String safeName) {
		String command = "select vdisk file=\"" + tempPath + "\\" + safeName + "\"\n"
				+ "detach vdisk";
		logger.info("detaching disk: " + safeName);
		runCommand(command);
	}
	
	/**
	 * Creates a diskpart script and runs it in the background and deletes the script afterwards
	 * @param command String containing commands delimited by '\n' except for the last line
	 */
	private static void runCommand(String command) {
		try {
			
			File flag = new File(tempPath + "\\greenFlag.vhd");
			
			// Delete if flag already exists
			if(flag.exists())
				flag.delete();
			
			PrintWriter pw  = new PrintWriter(tempPath + "\\runScript.txt");
			pw.write(command);
			pw.write("\ncreate vdisk file=\"" + tempPath + "\\greenFlag.vhd\" maximum=10");
			pw.close();
			
			File parentDir = new File(tempPath);
			File diskLog = new File(tempPath + "\\diskpart.log");
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(parentDir);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(diskLog));
			pb.command("cmd", "/c", "start", "/b", "diskpart", "/s", tempPath + "\\runScript.txt");
			Process p = pb.start();
			p.waitFor();
			
			int i=0;
			while(!flag.exists() && i<40) {
				i++;
				Thread.sleep(500);
			}
			if(!flag.exists()) {
				logger.severe("Running diskpart script failed. Did not get the green flag after 20sec");
			}
			//Wait for 2 secs for writing vhd file
			Thread.sleep(2000);
			flag.delete();
			new File(tempPath + "\\runScript.txt").delete();
			
		} catch (IOException e) {
			logger.severe("IOException caught while running command: " + e.getMessage());
		}  catch (InterruptedException e) {
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
			String command = "list volume";
			
			// Run the command
			runCommand(command);
			
			char biggestLetter = 'C';
			boolean ready = false;
			FileReader reader = new FileReader(tempPath + "\\diskpart.log");
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
	
	/**
	 * Opens the folder in which disk has been mounted given by Safe Label
	 * @param safeLabel String containing label of the Safe to be opened
	 */
	public static void openDisk(String safeLabel) {
		logger.info("Opening safe folder: " + safeLabel);
		List <File>files = Arrays.asList(File.listRoots());
		for (File f : files) {
	        String discription = FileSystemView.getFileSystemView().getSystemDisplayName(f);
	        if(!discription.isEmpty()) {
	        	String label = discription.substring(0, discription.length() - 5);
	        	if(label.equals(safeLabel)) {
	        		try {
						Runtime.getRuntime().exec("explorer.exe " + f.getAbsolutePath());
						return;
					} catch (IOException e) {
						logger.severe("IOException occured while opening drive folder: " + e.getMessage());
					}
	        	}
	        }
	    }
	}
	
	/**
	 * Deletes the specified safe vhd file
	 * @param safeName String containing the name of the safe in .vhd file
	 */
	public static void deleteSafe(String safeName) {
		// TODO
	}
	
	/**
	 * Deletes all .vhd files in temp folder. Use it in code Red situations
	 */
	public static void deleteAll() {
		// TODO
	}
	
}
