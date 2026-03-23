package taskmanager;
import java.io.File;
import taskmanager.core.TaskManager;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardUI extends JFrame {
    private TaskManager manager;
    private JTextArea outputArea = new JTextArea(15, 50);
    private JComboBox<String> appSelector = new JComboBox<>();
    private Timer refreshTimer;
    private final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

    public DashboardUI(TaskManager manager) {
        this.manager = manager;
        setTitle("Task Manager Dashboard");
        
        // Set larger font for macOS
        Font largeFont = new Font("SansSerif", Font.PLAIN, 16);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        JButton launchBtn = new JButton("Launch App");
        JButton closeBtn = new JButton("Close App");
        JButton viewRun = new JButton("View Running");
        JButton viewRecent = new JButton("View Recent");
        
        // Set button properties for better macOS rendering
        for (JButton btn : new JButton[]{launchBtn, closeBtn, viewRun, viewRecent}) {
            btn.setFont(largeFont);
            btn.setPreferredSize(new Dimension(150, 40));
            buttonPanel.add(btn);
        }
        
        // Output area setup
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(new CompoundBorder(
            new TitledBorder("Output"),
            new EmptyBorder(5, 5, 5, 5)));
            
        // Selector panel for closing apps
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectLabel = new JLabel("Select App:");
        selectLabel.setFont(largeFont);
        appSelector.setFont(largeFont);
        appSelector.setPreferredSize(new Dimension(300, 30));
        JButton closeSelectedBtn = new JButton("Close Selected");
        closeSelectedBtn.setFont(largeFont);
        selectPanel.add(selectLabel);
        selectPanel.add(appSelector);
        selectPanel.add(closeSelectedBtn);
        
        // Arrange panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(selectPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        // Enhanced button actions with Mac-specific features
        launchBtn.addActionListener(e -> {
            try {
                String app;
                if (IS_MAC) {
                    // Mac-specific file chooser for applications
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select Application");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setCurrentDirectory(new File("/Applications"));
                    
                    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        app = fileChooser.getSelectedFile().getAbsolutePath();
                    } else {
                        return; // User canceled
                    }
                } else {
                    app = JOptionPane.showInputDialog(this, "Enter application name/path to launch:", 
                                                     "Launch Application", JOptionPane.QUESTION_MESSAGE);
                    if (app == null || app.trim().isEmpty()) return;
                }
                
                if (manager.launchApp(app.trim())) {
                    outputArea.setText(app + " launched successfully.\n");
                    updateAppSelector();
                } else {
                    outputArea.setText("App already running or launch failed.\n");
                }
            } catch (Exception ex) {
                outputArea.setText("Error launching app: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        closeBtn.addActionListener(e -> {
            try {
                String app = JOptionPane.showInputDialog(this, "Enter application name to close:", 
                                                      "Close Application", JOptionPane.QUESTION_MESSAGE);
                if (app != null && !app.trim().isEmpty()) {
                    if (manager.closeApp(app.trim())) {
                        outputArea.setText(app + " closed successfully.\n");
                        updateAppSelector();
                    } else {
                        outputArea.setText("App not found or close operation failed.\n");
                    }
                }
            } catch (Exception ex) {
                outputArea.setText("Error closing app: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
        
        closeSelectedBtn.addActionListener(e -> {
            try {
                String selected = (String)appSelector.getSelectedItem();
                if (selected != null) {
                    // Extract app name from "appname (started at time)" format
                    String appName = selected.split(" \\(started at")[0].trim();
                    if (manager.closeApp(appName)) {
                        outputArea.setText(appName + " closed successfully.\n");
                        updateAppSelector();
                    } else {
                        outputArea.setText("Failed to close " + appName + ".\n");
                    }
                } else {
                    outputArea.setText("No application selected.\n");
                }
            } catch (Exception ex) {
                outputArea.setText("Error closing selected app: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        viewRun.addActionListener(e -> {
            try {
                java.util.List<String> apps = manager.getRunningApps();
                if (apps.isEmpty()) {
                    outputArea.setText("No applications are currently running.");
                } else {
                    StringBuilder sb = new StringBuilder("RUNNING APPLICATIONS:\n");
                    sb.append("=============================================\n");
                    for (String app : apps) {
                        sb.append(app).append("\n");
                    }
                    outputArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                outputArea.setText("Error retrieving running apps: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        viewRecent.addActionListener(e -> {
            try {
                java.util.List<String> apps = manager.getRecentApps();
                if (apps.isEmpty()) {
                    outputArea.setText("No applications in recent history.");
                } else {
                    StringBuilder sb = new StringBuilder("RECENT APPLICATIONS:\n");
                    sb.append("=============================================\n");
                    for (String app : apps) {
                        sb.append(app).append("\n");
                    }
                    outputArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                outputArea.setText("Error retrieving recent apps: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        // Set up refresh timer with reduced frequency for macOS (less CPU usage)
        refreshTimer = new Timer(8000, e -> {
            try {
                updateAppSelector();
            } catch (Exception ex) {
                System.err.println("Error in refresh timer: " + ex.getMessage());
            }
        });
        refreshTimer.start();
        
        // Initialize selector
        updateAppSelector();

        // Add window listener for clean exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanShutdown();
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle closing manually
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void updateAppSelector() {
        // Save selected item to restore after update
        String selected = (String)appSelector.getSelectedItem();
        
        appSelector.removeAllItems();
        for (String app : manager.getRunningApps()) {
            appSelector.addItem(app);
        }
        
        // Restore selection if possible
        if (selected != null) {
            for (int i = 0; i < appSelector.getItemCount(); i++) {
                if (appSelector.getItemAt(i).equals(selected)) {
                    appSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void cleanShutdown() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        // Close any resources if needed
        System.exit(0); // Ensure clean exit
    }
    
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}
