package taskmanager.core;

import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private Map<String, AppInfo> runningApps = new LinkedHashMap<>();
    private LinkedList<String> recentApps = new LinkedList<>();
    private final int RECENT_LIMIT = 5;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

    // Inner class to store app info
    public static class AppInfo {
        private String name;
        private LocalDateTime startTime;
        private Process process;
        private String processName;

        public AppInfo(String name, Process process) {
            this.name = name;
            this.startTime = LocalDateTime.now();
            this.process = process;
            
            // Extract actual process name for Mac
            this.processName = extractProcessName(name);
        }

        private String extractProcessName(String fullPath) {
            // For Mac, use the app name without path and extension if possible
            if (IS_MAC) {
                if (fullPath.endsWith(".app")) {
                    // Extract app name like "Safari.app" -> "Safari"
                    String filename = new File(fullPath).getName();
                    return filename.substring(0, filename.length() - 4);
                } else {
                    // Use just the filename without path
                    return new File(fullPath).getName();
                }
            }
            return fullPath;
        }

        public String getName() {
            return name;
        }
        
        public String getProcessName() {
            return processName;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public String getFormattedStartTime() {
            return startTime.format(timeFormatter);
        }

        public Process getProcess() {
            return process;
        }
        
        @Override
        public String toString() {
            return name + " (started at " + getFormattedStartTime() + ")";
        }
    }

    /**
     * Launches the specified application, with special handling for macOS.
     * 
     * @param app Name/path of the application to launch
     * @return true if app launched successfully, false otherwise
     */
    public boolean launchApp(String app) {
        if (runningApps.containsKey(app)) return false;
        
        try {
            Process process;
            if (IS_MAC && app.contains(".app")) {
                // Use "open" command for Mac applications
                process = Runtime.getRuntime().exec(new String[]{"open", app});
            } else if (IS_MAC && !app.startsWith("/")) {
                // For plain commands on Mac, try to execute directly
                process = Runtime.getRuntime().exec(app);
            } else {
                // Standard execution for full paths or Windows
                process = Runtime.getRuntime().exec(app);
            }
            
            AppInfo appInfo = new AppInfo(app, process);
            runningApps.put(app, appInfo);
            
            return true;
        } catch (IOException e) {
            System.err.println("App launch failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the specified application, with macOS-specific approach.
     * 
     * @param app Name of the application to close
     * @return true if application was closed successfully, false otherwise
     */
    public boolean closeApp(String app) {
        AppInfo appInfo = runningApps.get(app);
        if (appInfo == null) {
            return false;
        }
        
        boolean success = false;
        
        // Try to terminate the process directly first
        Process process = appInfo.getProcess();
        if (process != null && process.isAlive()) {
            try {
                process.destroy(); // Try gentle shutdown
                
                // Wait briefly for process to terminate
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    // If still running, try Mac-specific approach
                    if (IS_MAC) {
                        String processName = appInfo.getProcessName();
                        try {
                            // Try osascript first (more gentle approach)
                            String[] quitCmd = {"osascript", "-e", "tell application \"" + processName + "\" to quit"};
                            Process quitProcess = Runtime.getRuntime().exec(quitCmd);
                            success = quitProcess.waitFor(3, TimeUnit.SECONDS);
                            
                            // If osascript didn't work, try pkill
                            if (!success) {
                                Process killProcess = Runtime.getRuntime().exec(new String[]{"pkill", "-f", processName});
                                success = killProcess.waitFor(2, TimeUnit.SECONDS);
                            }
                        } catch (Exception e) {
                            System.err.println("Mac-specific close failed: " + e.getMessage());
                        }
                    }
                    
                    // If all else fails, try force destroy
                    if (!success) {
                        process.destroyForcibly();
                        success = process.waitFor(2, TimeUnit.SECONDS);
                    }
                } else {
                    success = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Process termination interrupted: " + e.getMessage());
            }
        }
        
        // Update data structures regardless of close method
        runningApps.remove(app);
        
        // Update recent apps list
        if (recentApps.contains(app)) {
            recentApps.remove(app);
        }
        recentApps.addFirst(app);
        if (recentApps.size() > RECENT_LIMIT) {
            recentApps.removeLast();
        }
        
        return true; // Assume success since we removed from our tracking
    }

    /**
     * Returns a list of all running applications with their start times.
     * 
     * @return List of formatted strings showing app names and start times
     */
    public List<String> getRunningApps() {
        List<String> result = new ArrayList<>();
        for (AppInfo app : runningApps.values()) {
            result.add(app.toString());
        }
        return result;
    }
    
    /**
     * Returns information about a specific running app.
     * 
     * @param app The app name to get info for
     * @return AppInfo object or null if app is not running
     */
    public AppInfo getAppInfo(String app) {
        return runningApps.get(app);
    }

   
    public List<String> getRecentApps() {
        return new ArrayList<>(recentApps);
    }

    public boolean isRunning(String app) {
        return runningApps.containsKey(app);
    }
}
