package bgu.spl171.net.api.bidi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {
	private Connections<Packet> connections;
	private int connectionId;
	private boolean logged = false;
	private boolean shouldTerminate = false;
	private byte[] allData = new byte[0];
	private String writeName = null;
	private String userPath = "";
	private ConcurrentLinkedQueue<DataPacket> dataToSend = new ConcurrentLinkedQueue<DataPacket>();
	private final int BLOCK = 128;
	private String username = "";

	// private static Random rand = new Random(2^20);

	@Override
	public boolean shouldTerminate() {
		return shouldTerminate;
	}

	@Override
	public void start(int connectionId, Connections<Packet> connections) {
		this.connections = connections;
		this.connectionId = connectionId;
	}

	@Override
	public void process(Packet message) {
		short opCode = message.getOp();
		String str = message.getString();
		String line = "";

		boolean flag = false;
		if (opCode == 11) { // Register
			// register op code - get users files from server
			String[] userMsg = str.split(" ");
			File users = new File("Users" + File.separator + "users.txt");
			username = userMsg[0];
			// search if username already exist - send error if it does
			try (BufferedReader br = new BufferedReader(new FileReader(users))) {
				while ((line = br.readLine()) != null) {
					String fileUserName = line.substring(0, line.indexOf(" "));
					if (fileUserName.equals(username)) {
						flag = true;
						break;
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			if (flag) {
				connections.send(connectionId, new ErrorPacket(((short) 7), "USERNAME ALREADY EXIST"));
			} else {
				try {
					// username is new - register it
					String salt = generateStringSalt();
					String enc = encrypt(userMsg[2].substring(0, 8), salt);
					String encryptUsername = encrypt(username, salt);
					String encryptUserdir = encrypt(username + "dir", salt);
					// create metadata text file for user and folder on server
					// init user's file count to 0
					String userMetaData = encrypt("0", salt) + "\n";
					byte[] MAC = mac(salt, encryptUsername);
					String mac = new String(MAC);
					mac = mac.replace('\n', 'X');
					mac = mac.replace('\r', 'X');
					String userDirData = encrypt("1", salt) + "\n" + encryptUsername + " " + mac + "\n";
					new File("Files/" + encryptUsername).mkdirs();
					new FileWriter(new File("Users/" + encryptUsername + ".txt"));
					new FileWriter(new File("Users/" + encryptUserdir + ".txt"));
					str = username + " " + salt + " " + enc + "\n";
					Files.write(Paths.get("Users/" + encryptUsername + ".txt"), userMetaData.getBytes(),
							StandardOpenOption.APPEND);
					Files.write(Paths.get("Users/" + encryptUserdir + ".txt"), userDirData.getBytes(),
							StandardOpenOption.APPEND);
					Files.write(Paths.get("Users" + File.separator + "users.txt"), str.getBytes(),
							StandardOpenOption.APPEND);
					connections.send(connectionId, new AckPacket((short) 0));
				} catch (Exception e) {
					System.out.println("CATCH");
				}
			}
		} else if (opCode == 7) { // logIn
			// check if user already logged in
			if (((ConnectionsImpl<Packet>) connections).isLogged(str))
				connections.send(connectionId, new ErrorPacket(((short) 7), "USER ALREADY LOGGED IN"));
			else {
				File users = new File("Users" + File.separator + "users.txt");
				String[] userMsg = str.split(" ");
				String salt;
				String pw;
				String filePw;
				String encrypted;
				line = "";
				username = userMsg[0];
				// search for user line with username salt and hash password
				try (BufferedReader br = new BufferedReader(new FileReader(users))) {
					while ((line = br.readLine()) != null) {
						String fileUserName = line.substring(0, line.indexOf(" "));
						if (fileUserName.equals(username))
							break;
					}
				} catch (IOException e) {

					e.printStackTrace();
				}
				if (line == null || line == "")
					connections.send(connectionId, new ErrorPacket(((short) 7), "USER NOT FOUND"));
				else {
					String[] userLine = line.split(" ");
					salt = userLine[1]; // get salt from file
					pw = userMsg[2].substring(0, 8); // password from user
					filePw = userLine[2]; // password from file
					encrypted = encrypt(pw, salt);
					// if password is correct keep checking otherwise send
					// "incorrect password"
					if (encrypted.equals(filePw)) {
						String encryptUsername = encrypt(username, salt);
						String encryptUserdir = encrypt(username + "dir", salt);
						int check = checkMetadata(encryptUsername, 0, salt);
						if (check == -1)
							return;
						check = checkMetadata(encryptUserdir, 1, salt);
						if (check == -1)
							return;

						// everything is O.K - user can LOGIN
						((ConnectionsImpl<Packet>) connections).connectByName(connectionId, message.getString());
						logged = true;
						connections.send(connectionId, new AckPacket((short) 0));
					} else
						connections.send(connectionId, new ErrorPacket(((short) 7), "INCORRECT PASSWORD"));
				}
			}
		} else if (logged) {
			switch (opCode) {
			case 1: {// RRQ
				userPath = FileSystems.getDefault().getPath("Files").toString() + "/";
				// encrypt path to desired file
				String[] strr = str.split("/");
				String salt = getSalt(username);
				userPath += encrypt(username, salt) + "/";
				String userP = "";
				for (int i = 0; i < strr.length - 1; i++) {
					userPath += encrypt(strr[i], salt) + "/";
					userP += strr[i] + "/";
				}
				String fileName = strr[strr.length - 1];
				FileSearch fileSearch = new FileSearch();
				String fileType = fileName.substring(fileName.indexOf("."));
				String encryptedFileName = encrypt(fileName.substring(0, fileName.indexOf(".")), salt);

				// search for file in all sub dirs of path
				fileSearch.searchDirectory(new File(userPath), encryptedFileName + fileType);

				int count = fileSearch.getResult().size();
				// if file isn't found...
				if (count == 0) {
					System.out.println("\nNo result found!");
					connections.send(connectionId, new ErrorPacket(((short) 1),
							"FILE " + fileName + " NOT FOUND IN ALL SUB DIRS OF " + username + "/" + userP));
				} else {
					// if there are more than 1 file found (multiple files with
					// the same name) , return first file found
					File f = new File(fileSearch.getResult().get(0));
					byte[] data = new byte[(int) f.length()];
					try {
						// start reading file
						FileInputStream fis = new FileInputStream(f);
						fis.read(data);
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					} // read file into bytes[]
					if (data.length > BLOCK) {
						for (int i = 1; i <= (data.length / BLOCK); i++) {
							dataToSend.add(new DataPacket((short) BLOCK, (short) i,
									Arrays.copyOfRange(data, ((i - 1) * BLOCK), (i * BLOCK))));
						}
					}
					dataToSend.add(new DataPacket((short) (data.length % BLOCK), (short) ((data.length / BLOCK) + 1),
							Arrays.copyOfRange(data, (data.length - (data.length) % BLOCK), data.length)));
					connections.send(connectionId, dataToSend.poll());
				}
				break;
			}
			case 2: {// WRQ
				// save file in user's path

				// encrypt path or use use's folder if path is empty

				String path = "";
				String[] userMsg = str.split(" ");
				if (userMsg.length == 2) {
					writeName = userMsg[0];
					username = userMsg[1];
				} else {
					writeName = userMsg[0];
					path = userMsg[1];
					username = userMsg[2];
				}

				boolean found = false;
				String salt = getSalt(username);
				String encryptUsername = encrypt(username, salt);
				String encPath = "";
				userPath = FileSystems.getDefault().getPath("Files") + "/" + encryptUsername + "/";
				if (path != "") {
					String[] strr = path.split("/");
					for (int i = 0; i < strr.length; i++) {
						String enc = encrypt(strr[i], salt);
						userPath += enc + "/";
						try {
							byte[] MAC = mac(salt, enc);
							String mac2 = new String(MAC);
							mac2 = mac2.replace('\n', 'X');
							mac2 = mac2.replace('\r', 'X');
							encPath += enc + " " + mac2 + "\n";
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				int indexOfPoint = writeName.indexOf(".");
				String name = writeName.substring(0, indexOfPoint);
				String type = writeName.substring(indexOfPoint);
				String encFileName = encrypt(name, salt) + type;

				// create encrypted folder to path

				File folder = new File(userPath);
				File createFolder = new File(userPath + encFileName);
				createFolder.getParentFile().mkdirs();

				try {
					updateUsersDirData(encPath, salt, encrypt(username + "dir", salt));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				File[] listOfFiles = folder.listFiles();
				// check if we need to override file
				if (listOfFiles != null) {
					for (File file : listOfFiles) {
						if (file.isFile() && file.getName().equals(encFileName)) {
							found = true;
							if (!file.delete())
								connections.send(connectionId,
										new ErrorPacket((short) 2, "VIOLATION ACCURED - CANNOT DELETE"));
						}
					}
				}
				// - if we override we need to delete it and update user
				// metadata
				if (found) {
					try {
						updateUserMetadata(false, writeName, 0);
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
				connections.send(connectionId, new AckPacket((short) 0));
				break;
			}
			case 3: { // DATA
				// write data to user's encrypted folder
				if (writeName != null) {
					String salt = getSalt(username);
					String encryptUsername = encrypt(username, salt);
					File folder = new File("Files/" + encryptUsername);
					long space = folder.getFreeSpace();
					int lastdataSize = 0;
					byte[] data = (((DataPacket) message).getData());
					lastdataSize = allData.length;
					allData = Arrays.copyOf(allData, lastdataSize + data.length);
					System.arraycopy(data, 0, allData, lastdataSize, data.length);
					if (allData.length > space) {
						connections.send(connectionId, new ErrorPacket((short) 3, "NOT ENOUGH SPACE"));
					}
					connections.send(connectionId, new AckPacket(((DataPacket) message).getBlockNum()));
					if (data.length < BLOCK) {
						try {
							// if it's the last block we write our accumulated
							// data
							String fileType = writeName.substring(writeName.indexOf("."));
							String encryptedFileName = encrypt(writeName.substring(0, writeName.indexOf(".")), salt);
							FileOutputStream fout = new FileOutputStream(userPath + encryptedFileName + fileType);
							fout.write(allData);
							fout.close();
							// now we need to update user's metadata
							int userFileCount = 0;
							File userData = new File("Users/" + encryptUsername + ".txt");
							BufferedReader br = new BufferedReader(new FileReader(userData));
							String firstLine = br.readLine() + "\n";
							String allText = "";
							while ((line = br.readLine()) != null)
								allText += line + "\n";
							br.close();
							try {
								userFileCount = Integer.parseInt(decrypt(firstLine));
							} catch (NumberFormatException nfe) {
								System.out.println("Not a Number");
								connections.send(connectionId,
										new ErrorPacket(((short) 7), "USER'S FILE COUNT MODIFIED! SERVER IS HOSTILE!"));
								br.close();
								return;
							}
							updateUserMetadata(true, writeName, allData.length);
							allData = new byte[0];

						} catch (Exception e) {
							connections.send(connectionId,
									new ErrorPacket((short) 2, "VIOLATION ACCURED - CANNOT WRITE"));
						}
						connections.broadcast(new BCastPacket((byte) 1, writeName));
					}
				}
				break;
			}
			case 4: {
				try {
					DataPacket pack = dataToSend.poll();
					if (((AckPacket) message).getBlockNum() == pack.getBlockNum() - 1)
						connections.send(connectionId, pack);
					else
						connections.send(connectionId, new ErrorPacket((short) 0, "BLOCK NUMBER DOES NOT MATCH"));
				} catch (NullPointerException e) {
				}
				break;
			}
			case 5: {
				dataToSend.clear();
				connections.send(connectionId, new ErrorPacket((short) 0, "HMAC FAILED FILE STOPPED SENDING"));
				break;
			}
			case 6: { // DIRECTORY
				String names = "";
				String salt = getSalt(username);
				String encryptUsername = encrypt(username, salt);

				// search for files in all encrypted user's folders and decrypt
				// file names

				File folder = new File(
						FileSystems.getDefault().getPath("Files") + File.separator + encryptUsername + "/");
				ArrayList<File> files = new ArrayList<File>();
				listf(folder.toString(), files);

				for (int i = 0; i < files.size(); i++) {
					String fname = files.get(i).getName();
					String[] path = files.get(i).getPath().split("/");
					int indexOfPoint = fname.indexOf(".");
					String name = fname.substring(0, indexOfPoint);
					String type = fname.substring(indexOfPoint);
					for (int j = 1; j < path.length - 1; j++) {
						names += decrypt(path[j]) + "/";
					}
					names += decrypt(name) + type + '\0';
				}

				byte[] nameBytes = names.getBytes();
				for (int i = 1; i < (nameBytes.length / BLOCK); i++) {
					connections.send(connectionId, new DataPacket((short) BLOCK, (short) i,
							Arrays.copyOfRange(nameBytes, ((i - 1) * BLOCK), (i * BLOCK) - 1)));
				}
				connections.send(connectionId,
						new DataPacket((short) (nameBytes.length % BLOCK), (short) ((nameBytes.length / BLOCK) + 1),
								Arrays.copyOfRange(nameBytes, (nameBytes.length) - ((nameBytes.length) % BLOCK),
										nameBytes.length)));
				break;
			}
			case 8:

			{ // delete request
				boolean found = false;
				String salt = getSalt(username);
				String encryptUsername = encrypt(username, salt);

				// get correct path to file , delete it and update user's
				// metadata

				userPath = FileSystems.getDefault().getPath("Files") + "/" + encryptUsername + "/";
				String[] strr = str.split("/");
				for (int i = 0; i < strr.length - 1; i++)
					userPath += encrypt(strr[i], salt) + "/";

				String fileName = strr[strr.length - 1];
				int indexOfPoint = fileName.indexOf(".");
				String name = fileName.substring(0, indexOfPoint);
				String type = fileName.substring(indexOfPoint);
				String encFileName = encrypt(name, salt) + type;

				File folder = new File(userPath);
				File[] listOfFiles = folder.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile() && file.getName().equals(encFileName)) {
						found = true;
						if (!file.delete())
							connections.send(connectionId,
									new ErrorPacket((short) 2, "VIOLATION ACCURED - CANNOT DELETE"));
					}
				}
				if (found) {
					try {
						updateUserMetadata(false, fileName, 0);
					} catch (Exception e) {

						e.printStackTrace();
					}
					connections.send(connectionId, new AckPacket((short) 0));
					connections.broadcast(new BCastPacket((byte) 0, message.getString()));
				} else
					connections.send(connectionId, new ErrorPacket((short) 1, "FILE NOT FOUND"));
				break;
			}
			case 12:

			{ // rename request
				// get correct path to user's file and update metadata
				String[] strr = str.split(" ");
				String[] path = strr[0].split("/");
				boolean found = false;
				String salt = getSalt(username);
				String encryptUsername = encrypt(username, salt);

				userPath = FileSystems.getDefault().getPath("Files") + "/" + encryptUsername + "/";
				for (int i = 0; i < path.length - 1; i++)
					userPath += encrypt(path[i], salt) + "/";

				String oldFileName = path[path.length - 1];
				String newFileName = strr[1];
				username = strr[2];
				int indexOfPoint = oldFileName.indexOf(".");
				String name = oldFileName.substring(0, indexOfPoint);
				String type = oldFileName.substring(indexOfPoint);
				String encOldFileName = encrypt(name, salt) + type;
				String newName = newFileName.substring(0, newFileName.indexOf("."));
				String encNewFileName = encrypt(newName, salt) + type;
				int fileSize = 0;

				File folder = new File(userPath);
				File newFile = new File(userPath + encNewFileName);
				File[] listOfFiles = folder.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile() && file.getName().equals(encOldFileName)) {
						if (newFile.exists())
							connections.send(connectionId, new ErrorPacket((short) 1, "FILE EXISTS"));
						else if (!file.renameTo(newFile))
							connections.send(connectionId,
									new ErrorPacket((short) 2, "VIOLATION ACCURED - CANNOT CHANGE"));
						else {
							found = true;
							fileSize = (int) newFile.length();
						}
					}
				}
				if (found) {
					try {
						fixUserMetadata(encOldFileName, encNewFileName, fileSize);
					} catch (Exception e) {

						e.printStackTrace();
					}
					connections.send(connectionId, new AckPacket((short) 0));
					connections.broadcast(new BCastPacket((byte) 3, message.getString()));
				} else
					connections.send(connectionId, new ErrorPacket((short) 1, "FILE NOT FOUND"));
				break;
			}
			case 10:

			{ // disconnect
				this.shouldTerminate = true;
				this.logged = false;
				connections.send(connectionId, new AckPacket((short) 0));
				connections.disconnect(connectionId);
				break;
			}
			default: {
				connections.send(connectionId, new ErrorPacket((short) 4, "UNKNOWN OPCODE"));
			}
			}
		} else {
			if (opCode > 14 || opCode < 1)
				connections.send(connectionId, new ErrorPacket((short) 4, "UNKNOWN OPCODE"));
			else
				connections.send(connectionId, new ErrorPacket((short) 6, "USER NOT LOGGED IN"));
		}
	}

	private void updateUsersDirData(String encPath, String salt, String dataDirname) throws Exception {
		String[] lines = encPath.split("\n");
		String line = "";
		BufferedReader br = null;
		String allText = "";
		int amount;


		for (int i = 0; i < lines.length; i++) {
			br = new BufferedReader(new FileReader(new File("Users/" + dataDirname + ".txt")));
			br.readLine();
			amount = 0;
			allText = "";
			while ((line = br.readLine()) != null) {
				allText += line + "\n";
				amount++;
			}
			if (!allText.contains(lines[i])) {
				allText += lines[i] + "\n";
				amount++;
			}
			allText = encrypt(Integer.toString(amount), salt) + "\n" + allText;
			br.close();
			FileOutputStream fileOut = new FileOutputStream("Users/" + dataDirname + ".txt");
			fileOut.write(allText.getBytes());
			fileOut.close();
		}
	}

	// function to generate salt
	public static String generateStringSalt() {
		byte[] salt = new byte[8];
		int randseed = (int) (Math.random() * Math.pow(2, 20));
		Random rand = new Random(randseed);
		rand.nextBytes(salt);
		return salt.toString();
	}

	// function to get file names on given path
	public void listf(String directoryName, ArrayList<File> files) {
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();

		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listf(file.getPath(), files);
			}
		}
	}

	public void listd(String directoryName, ArrayList<File> files) {
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isDirectory()) {
				files.add(file);
				listd(file.getPath(), files);
			}
		}
	}

	public static String encrypt(String enc, String salt) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(salt.getBytes()) + encoder.encode(enc.getBytes());
	}

	public static String decrypt(String encstr) {
		if (encstr.length() > 16) {
			String cipher = encstr.substring(16);
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				return new String(decoder.decodeBuffer(cipher));
			} catch (IOException e) {

			}

		}
		return null;
	}

	// utility function to get user salt from users.txt
	// returns the salt of a given username
	private String getSalt(String user) {
		String salt;
		String line = "";
		try (BufferedReader br = new BufferedReader(new FileReader(new File("Users/users.txt")))) {
			while ((line = br.readLine()) != null) {
				String fileUserName = line.substring(0, line.indexOf(" "));
				if (fileUserName.equals(user))
					break;
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		String[] userLine = line.split(" ");
		salt = userLine[1]; // get salt from file

		return salt;

	}

	// mac function
	public static byte[] mac(String key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		return sha256_HMAC.doFinal(data.getBytes("UTF-8"));
	}

	// this function check if a file was renamed or it's size changed
	private int checkMac(String encFileName, String encryptedUserName, String salt, String fileSize) throws Exception {
		int ans = 0;
		String line = "";
		File userMetaData = new File("Users" + File.separator + encryptedUserName + ".txt");
		try (BufferedReader br = new BufferedReader(new FileReader(userMetaData))) {
			br.readLine(); // read first line - its encrypted amount of files
			// each line from now on is <filename> <filesize> <mac>
			while ((line = br.readLine()) != null) {
				String[] userLine = line.split(" ");
				String fname = userLine[0];
				String size = userLine[1];
				String mac = "";

				for (int i = 2; i < userLine.length; i++)
					mac += " " + userLine[i];
				mac = mac.substring(1);

				int indexOfPoint = fname.indexOf(".");
				String name = fname.substring(0, indexOfPoint);
				indexOfPoint = encFileName.indexOf(".");
				String encFName = encFileName.substring(0, indexOfPoint);

				// we got mac from line
				// we can check if it matches file's mac
				// same goes for size

				byte[] MAC = mac(salt, name);
				String mac2 = new String(MAC);
				mac2 = mac2.replace('\n', 'X');
				mac2 = mac2.replace('\r', 'X');
				MAC = mac(salt, encFName);
				String mac3 = new String(MAC);
				mac3 = mac3.replace('\n', 'X');
				mac3 = mac3.replace('\r', 'X');

				if (!(fname.equals(encFileName) && mac2.contains(mac) && mac3.equals(mac2) && mac3.contains(mac)))
					ans = 1;
				else if (!(fileSize.equals(size)))
					return 2;
				else
					return 0;

			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		return ans;
	}

	private int checkMacForDirs(String encFileName, String encryptedUserName, String salt) throws Exception {
		int ans = 0;
		String line = "";
		File userMetaData = new File("Users" + File.separator + encryptedUserName + ".txt");
		try (BufferedReader br = new BufferedReader(new FileReader(userMetaData))) {
			br.readLine(); // read first line - its encrypted amount of files
			// each line from now on is <filename> <mac>
			while ((line = br.readLine()) != null) {
				String[] userLine = line.split(" ");
				String fname = userLine[0];
				String mac = "";

				for (int i = 1; i < userLine.length; i++)
					mac += " " + userLine[i];
				mac = mac.substring(1);

				// we got mac from line
				// we can check if it matches dir's mac

				byte[] MAC = mac(salt, fname);
				String mac2 = new String(MAC);
				mac2 = mac2.replace('\n', 'X');
				mac2 = mac2.replace('\r', 'X');

				if (!(fname.equals(encFileName) && mac2.contains(mac)))
					ans = 3;
				else
					return 0;

			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		return ans;
	}

	private void updateUserMetadata(boolean inc, String fileName, int fileSize) throws Exception {
		int userFileCount = 0;
		// gather all information needed
		String salt = getSalt(username);
		String encryptUsername = encrypt(username, salt);
		String fileType = fileName.substring(fileName.indexOf("."));
		String encryptedFileName = encrypt(fileName.substring(0, fileName.indexOf(".")), salt);
		String encFile = encryptedFileName + fileType;
		String line = "";
		String path = "Users/" + encryptUsername + ".txt";
		String fileSizeStr = encrypt(Integer.toString(fileSize), salt);
		File userData = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(userData));
		String firstLine = br.readLine() + "\n";
		String allText = "";

		userFileCount = Integer.parseInt(decrypt(firstLine));

		if (inc) { // add new file to user metadata
			// update file count and add new file
			userFileCount++;
			while ((line = br.readLine()) != null)
				allText += line + "\n";

			br.close();
			byte[] MAC = mac(salt, encryptedFileName);
			String mac = new String(MAC);
			mac = mac.replace('\n', 'X');
			mac = mac.replace('\r', 'X');
			allText = encrypt(Integer.toString(userFileCount), salt) + "\n" + allText;
			allText += encFile + " " + fileSizeStr + " " + mac;
			FileOutputStream fileOut = new FileOutputStream(path);
			fileOut.write(allText.getBytes());
			fileOut.close();

		} else {
			// we want to remove a file
			// we need to decrement fileCount and delete the line that
			// represented the file that we deleted
			userFileCount--;
			boolean found = false;
			byte[] MAC = mac(salt, encryptedFileName);
			String mac = new String(MAC);
			mac = mac.replace('\n', 'X');
			mac = mac.replace('\r', 'X');
			while ((line = br.readLine()) != null) {
				if (line.indexOf(encFile) != -1 && !found) {
					found = true;
					continue;
				}
				allText += line + "\n";
			}
			br.close();
			allText = encrypt(Integer.toString(userFileCount), salt) + "\n" + allText;
			FileOutputStream fileOut = new FileOutputStream(path);
			fileOut.write(allText.getBytes());
			fileOut.close();

		}
	}

	// used in rename a file
	private void fixUserMetadata(String encOldFileName, String encNewFileName, int fileSize) throws Exception {
		// gather all info needed
		String salt = getSalt(username);
		String encryptUsername = encrypt(username, salt);
		String encryptedNewFileName = encNewFileName.substring(0, encNewFileName.indexOf("."));
		String line = "";
		String path = "Users/" + encryptUsername + ".txt";
		String fileSizeStr = encrypt(Integer.toString(fileSize), salt);
		File userData = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(userData));
		String allText = "";
		byte[] MAC = mac(salt, encryptedNewFileName);
		String mac = new String(MAC);
		mac = mac.replace('\n', 'X');
		mac = mac.replace('\r', 'X');
		String newLine = encNewFileName + " " + fileSizeStr + " " + mac;

		// we got encrypted line that describes the file
		// replace old line with new line

		boolean found = false;

		while ((line = br.readLine()) != null) {
			if (line.indexOf(encOldFileName) != -1 && !found) {
				found = true;
				continue;
			}
			allText += line + "\n";
		}
		allText += newLine;
		br.close();
		FileOutputStream fileOut = new FileOutputStream(path);
		fileOut.write(allText.getBytes());
		fileOut.close();

	}

	private int checkMetadata(String filename, int filesType, String salt) {

		String line = "";
		String type = "";
		if (filesType == 1)
			type = "dir";
		String encryptUserFile = encrypt(username + type, salt);
		int userFileCount = 0;
		int userFile = 0;

		File userData = new File("Users/" + encryptUserFile + ".txt");
		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(userData));
			line = br.readLine();
			try {
				userFileCount = Integer.parseInt(decrypt(line));
			} catch (NumberFormatException nfe) {
				System.out.println("NaN");
				if (filesType == 0)
					connections.send(connectionId,
							new ErrorPacket(((short) 7), "USER'S FILE COUNT MODIFIED! SERVER IS HOSTILE!"));
				else
					connections.send(connectionId,
							new ErrorPacket(((short) 7), "USER'S FOLDER COUNT MODIFIED! SERVER IS HOSTILE!"));
				br.close();
				return -1;
			}
			// count how many files stores on metadata file
			while ((line = br.readLine()) != null) {
				userFile++;
			}
			br.close();

			// if file can't be opened it was deleted!
		} catch (IOException e) {
			connections.send(connectionId,
					new ErrorPacket(((short) 7), "USER METADATA FILE WAS DELETED! SERVER IS HOSTILE!"));
			return -1;
		}
		// count how many files are actually stored on server
		File folder = new File(
				FileSystems.getDefault().getPath("Files") + File.separator + encrypt(username, salt) + "/");

		ArrayList<File> files = new ArrayList<File>();
		if (filesType == 0)
			try {
				listf(folder.toString(), files);

			} catch (NullPointerException ex) {
				connections.send(connectionId,
						new ErrorPacket(((short) 7), "USER'S FOLDER NAME CHANGED! SERVER IS HOSTILE!"));
				return -1;
			}
		else {
			listd(folder.toString(), files);
			files.add(new File("Files/" + encrypt(username, salt)));
		}
		int filesOnServer = files.size();		
		// all three counters should be equal - otherwise a file
		// was deleted!
		if (userFile != userFileCount || userFile != filesOnServer || userFileCount != filesOnServer) {
			if (filesType == 0)
				connections.send(connectionId,
						new ErrorPacket(((short) 7), "BAD FILES COUNT! A FILE WAS DELETED! SERVER IS HOSTILE!"));
			else
				connections.send(connectionId,
						new ErrorPacket(((short) 7), "BAD FOLDERS COUNT! A FOLDER WAS DELETED! SERVER IS HOSTILE!"));
			return -1;
		}

		int check = 0;
		for (int i = 0; i < files.size() && check == 0; i++) {
			String fname = files.get(i).getName();
			try {
				// checkMac tests whether a file was renamed or
				// it's size changed
				// it returns 0 if everything O.K
				// returns 1 if file was renamed
				// returns 2 if file size changed
				if (filesType == 0)
					check = checkMac(fname, encryptUserFile, salt, encrypt(Long.toString(files.get(i).length()), salt));
				else
					check = checkMacForDirs(fname, encryptUserFile, salt);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		if (check == 1) {
			connections.send(connectionId, new ErrorPacket(((short) 7), "FILE WAS RENAMED! SERVER IS HOSTILE!"));
			return -1;
		} else if (check == 2) {
			connections.send(connectionId, new ErrorPacket(((short) 7), "FILE SIZE CHANGED! SERVER IS HOSTILE!"));
			return -1;
		} else if (check == 3) {
			connections.send(connectionId, new ErrorPacket(((short) 7), "FOLDER WAS RENAMED! SERVER IS HOSTILE!"));
			return -1;
		}

		return 0;
	}

}
