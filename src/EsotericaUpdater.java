import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

public class EsotericaUpdater {
	public static enum OS { Windows, Mac, Other }
	public static final String title = "EsotericaCraft Updater";
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// get version
		HashMap<String, Object> map;
		try {
			map = new Gson().fromJson(getVersion("http://darksundev.com:25566/version"), HashMap.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// parse results
		String version = (String) map.get("version");
		String filename = String.format("EsotericaCraft-%s.jar", String.valueOf(version));
		String downloadUrl= (String) map.get("file");
		
		// get minecraft install directory
		String path = null;
		switch (getOS()) {
		case Windows:
			path = System.getenv("APPDATA") + "\\.minecraft\\mods";
			break;
		case Mac:
			path = System.getProperty("user.home") + "/Library/Application Support/minecraft/mods";
			break;
		case Other:
			Popup.warning("Aborting install! You are not using a supported operating system.");
			return;
		}

		// do update
		if (doUpdate(filename, downloadUrl, new File(path))) {
			Popup.notice("Update installed successfully!");
		}
		else {
			Popup.warning("Download failed!");
		}
	}
	
	public static OS getOS() {
		String name = System.getProperty("os.name").toLowerCase();
		if (name.indexOf("win") >= 0) {
			return OS.Windows;
		}
		else if (name.indexOf("mac") >= 0) {
			return OS.Mac;
		}
		else {
			return OS.Other;
		}
	}
	
	public static String getVersion(String address) throws Exception {
		URL url = new URL(address);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line);
		}
		reader.close();
		return result.toString();
	}
	
	public static boolean doUpdate(String filename, String download, File installPath) {
		String[] pathnames = installPath.list();
		boolean downloadNeeded = true;
		boolean alreadyInstalled = false;
		for (String name : pathnames) {
			if (name.contains("EsotericaCraft")) {
				if (name.equals(filename)) {
					// up to date
					Popup.info(String.format("Already up to date!%sFound: %s", System.lineSeparator(), name));
					downloadNeeded = false;
					alreadyInstalled = true;
				}
				else {
					// out of date version found
					Popup.warning(String.format("Out of date!%sFound: %s%sNewest Version: %s", System.lineSeparator(), name, System.lineSeparator(), filename));
					new File(installPath, name).delete(); // delete old version
					alreadyInstalled = true;
				}
			}
		}
		if (!alreadyInstalled) {
			Popup.info("No existing version found. Press 'OK' to install latest version");
		}
		if (downloadNeeded) {
			try {
				FileUtils.copyURLToFile(new URL(download), new File(installPath, filename));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			Popup.notice("Download complete...");
		}
		
		return true;
	}

	public static class Popup {
		public static void notice(String message) {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.DEFAULT_OPTION);
		}
		public static void warning(String message) {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.OK_OPTION);
		}
		public static void question(String message) {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
		}
		public static void info(String message) {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
