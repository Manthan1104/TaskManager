package taskmanager.io;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class UserManager {
    // Use user's home directory for data files on Mac
    private static final String DATA_DIR = "data";
    private static final String FILE_NAME = "users.txt";
    private static final String FILE_PATH;
    
    static {
        // Initialize file path based on current working directory
        String workingDir = System.getProperty("user.dir");
        FILE_PATH = Paths.get(workingDir, DATA_DIR, FILE_NAME).toString();
        
        // Ensure data directory exists
        try {
            Files.createDirectories(Paths.get(workingDir, DATA_DIR));
            
            // Create users file if it doesn't exist
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.createFile(Paths.get(FILE_PATH));
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory or file: " + e.getMessage());
        }
    }

    public static boolean register(String user, String pass) {
        if (user == null || user.trim().isEmpty() || pass == null) {
            return false;
        }
        
        Map<String, String> users = readUsers();
        if (users.containsKey(user)) return false;
        
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
            fw.write(user + "," + pass + "\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public static boolean authenticate(String user, String pass) {
        if (user == null || pass == null) {
            return false;
        }
        
        Map<String, String> users = readUsers();
        return pass.equals(users.get(user));
    }

    private static Map<String, String> readUsers() {
        Map<String, String> map = new HashMap<>();
        
        try {
            // Ensure file exists
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            
            // Read file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) map.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
        }
        
        return map;
    }
}
