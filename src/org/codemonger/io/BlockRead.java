package org.codemonger.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class BlockRead {
	
	private static final String REV_STR = "0.5 rev20050613";
	private static final String AUTHOR_STR = "dwilkin";
	
	private static final String CMD_EXIT = "EXIT";
	private static final String CMD_HELP = "HELP";
	private static final String CMD_CMDS = "?";
	private static final String CMD_READ = "READ";
	private static final String CMD_CONT = "CONT";
	private static final String CMD_SET = "SET";
	private static final String CMD_GET = "GET";
	private static final String CMD_SIZE = "SIZE";
	private static final String CMD_FIND = "FIND";
	
	private static void displayHelp() {
		System.out.println("\nCommands for BlockReader " + REV_STR + " are as follows:\n");
		System.out.println("\texit\t\t\t- exit BlockReader");
		System.out.println("\thelp\t\t\t- displays this help screen");
		System.out.println("\tread <byte> <lines>\t- reads a block of text starting at the specified");
		System.out.println("\t\tbyte location until the specified number of lines have been read");
		System.out.println("\tcont <lines>\t\t- continue reading the specified number of lines");
		System.out.println("\t\tfrom the current position");
		System.out.println("\tset <byte>\t\t- positions the file pointer to the specified byte,");
		System.out.println("\t\twhere -n is the bytes from the tail, +n is the bytes from the head");
		System.out.println("\tget\t\t\t- displays the current position of the file pointer");
		System.out.println("\tsize\t\t\t- displays the size of the current file in bytes");
		System.out.println("\tfind <text>\t\t- searches for the specified text starting from the");
		System.out.println("\t\tcurrent position to the end of the file, restores the file pointer");
		System.out.println("\t\tto the previous position\n");
	}
	
	private static String promptForCommand() throws IOException {
		System.out.print("\nread> ");
		BufferedReader prompt = new BufferedReader(new InputStreamReader(System.in));
		return prompt.readLine();
	}
	
	private static void displayUsage() {
		System.out.println("BlockRead " + REV_STR + " " + AUTHOR_STR);
		System.out.println("Syntax:\tBlockRead <filename>\n");
		System.out.println("\taccesses the specified text file and enables reading of the file");
		System.out.println("\tfrom a specified position for a specified number of lines.\n");
	}
	
	public static void main(String args[]) throws IOException {
		long currentPos = -1;
		if (args.length == 0) {
			System.out.println("[ERR] Nothing to read.\n");
			displayUsage();
		}
		else {
			File filename = new File(args[0]);
			if (!filename.exists()) {
				System.out.println("[ERR] Specified file, '" + args[0] + "', cannot be found.");
			} else {
				String cmd;
				do {
					cmd = promptForCommand();
					if (cmd.equalsIgnoreCase(CMD_HELP) || cmd.equals(CMD_CMDS)) {
						displayHelp();
					}
					else if (cmd.equalsIgnoreCase(CMD_GET)) {
						if (currentPos == -1) {
							System.out.println("File pointer has not been set.");
						} else {
							System.out.println("File pointer is currently located at byte " + 
								currentPos);
						}
					}
					else if (cmd.equalsIgnoreCase(CMD_SIZE)) {
						System.out.println("The current file size is " + filename.length());
					}
					else if (cmd.toUpperCase().startsWith(CMD_SET)) {
						int pos = cmd.indexOf(' ');
						String byteStart = cmd.substring(pos + 1);
						long start = 0;
						try {
							start = Long.parseLong(byteStart.trim());
							if (+(start) > filename.length()) {
								System.out.println("Can't reposition past file boundary.");
							}
							else {
								if (byteStart.startsWith("-")) {
									currentPos = filename.length();
									System.out.println("Repositioning relative to tail of file.");
								} else {
									currentPos = 0;
									System.out.println("Repositioning relative to head of file.");
								}
								currentPos += start;
							}
						}
						catch (NumberFormatException nfe) {
							System.out.println("[ERR] Unrecognized byte position.");
							continue;
						}
					}
					else if (!cmd.equalsIgnoreCase(CMD_EXIT)) {
						
						// process a command that will read the file contents
						RandomAccessFile file = new RandomAccessFile(filename, "r");
						
						if (cmd.toUpperCase().startsWith(CMD_READ)) {
							// process READ command
							int pos = cmd.indexOf(' ');
							int pos2 = cmd.indexOf(' ', pos + 1);
							if (pos == -1) {
								System.out.println("[ERR] Unrecognized starting byte address.");
							} 
							else if (pos2 == -1) {
								System.out.println("[ERR] Unrecognized line count.");
							}
							else {
								String byteStart = cmd.substring(pos, pos2);
								String lineCount = cmd.substring(pos2 + 1);
								long start = 0;
								long lines = 0;
								try {
									start = Long.parseLong(byteStart.trim());
									lines = Long.parseLong(lineCount.trim());
		
									System.out.println("reading " + lineCount + " lines starting from byte " + byteStart + "\n");
									file.seek(start);
									for (long i = 0; i < lines; i++) {
										System.out.println(file.readLine());
									}
									currentPos = file.getFilePointer();
								}
								catch (NumberFormatException nfe) {
									System.out.println("[ERR] Unrecognized file position or line count.");
								}
							}
						}
						else if (cmd.toUpperCase().startsWith(CMD_CONT)) {
							// process CONT command
							int pos = cmd.indexOf(' ');
							if (pos == -1) {
								System.out.println("[ERR] Line count not specified.");
							}
							else {
								String lineCount = cmd.substring(pos + 1);
								long lines = 0;
								try {
									lines = Long.parseLong(lineCount.trim());
									
									if (currentPos == -1) {
										System.out.println("Starting from head of file.");
										currentPos = 0;
									}
									file.seek(currentPos);
									System.out.println("reading " + lineCount + " lines starting from current position\n");
									String line = "";
									for (long i = 0; i < lines && line != null; i++) {
										if ((line = file.readLine()) != null)
											System.out.println(line);
									}
									if (line == null)
										System.out.println("\n[EOF]");
									currentPos = file.getFilePointer();
								}
								catch (NumberFormatException nfe) {
									System.out.println("[ERR] Unrecognized line count.");
								}
							}
						}
						else if (cmd.toUpperCase().startsWith(CMD_FIND)) {
							// process FIND command
							int pos = cmd.indexOf(' ');
							if (pos == -1) {
								System.out.println("[ERR] Search text not specified.");
							}
							else {
								String searchText = cmd.substring(pos + 1);
								
								if (currentPos == -1) {
									System.out.println("Starting from head of file.");
									currentPos = 0;
								}
								file.seek(currentPos);
								System.out.println("searching for first occurrence of '" + searchText + "' from byte " + currentPos);
								int blocksRead = 0;
								int count = 0;
								byte[] block = new byte[300];
								while ((count = file.read(block)) != -1) {
									blocksRead++;
									if (blocksRead % 100 == 0) {
										if (blocksRead % 10000 == 0) {
											System.out.println("");
											if (blocksRead > Integer.MAX_VALUE - 15000) {
												blocksRead = 0;
											}
										}
										System.out.print(".");
									}
									if ((new String(block)).indexOf(searchText) != -1) {
										System.out.println("\nFound at: " + (file.getFilePointer() - count));
										break;
									}
								}
								if (count == -1)
									System.out.println("\n[EOF]");
							}
						}
	
						file.close();
					}
				} while (!cmd.equalsIgnoreCase(CMD_EXIT));
			}
		}
	}

}
