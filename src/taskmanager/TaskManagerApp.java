package taskmanager;

import taskmanager.core.TaskManager;
import javax.swing.*;

public class TaskManagerApp {
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Task Manager");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> new LoginUI(new TaskManager()));
    }
}
