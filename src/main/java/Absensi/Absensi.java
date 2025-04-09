package Absensi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatLightLaf;

import Components.CustomPanel;
import Components.CustomTable.CustomTable;
import DataBase.QueryExecutor;
import Global.UserSessionCache;

public class Absensi extends JFrame {

    UserSessionCache userSessionCache = new UserSessionCache();
    private JButton btnCheckIn, btnCheckOut, btnIjin;
    private String uuid = userSessionCache.getUUID();
    private JLabel lblDateTime;
    private DefaultTableModel model;
    private JScrollPane tableScrollPane;
    private JLabel lblCheckInTime, lblCheckOutTime;
    private boolean ijinClickedToday = false; // Flag to track if "Ijin" has been clicked today
    private boolean checkInClickedToday = false; // Flag to track if "Check In" has been clicked today
    private boolean checkOutClickedToday = false; // Flag to track if "Check Out" has been clicked today
    Object[][] data = {};

    public Absensi() {
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Initialize labels before accessing them
        lblCheckInTime = new JLabel("Check In Today: -");
        lblCheckOutTime = new JLabel("Check Out Today: -");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Data Panel (Displays absensi details)
        CustomPanel detailPanel = createDetailPanel();

        // Table Panel (Displays list of absensi)
        tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(detailPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Start the timer to update the date and time label
        startTimer();

        // Load the state from the database
        refreshTableData(uuid);
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columnNames = new String[]{"ID", "Nama", "Waktu Masuk", "Waktu Pulang", "Status"};

        // Table model
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No cells are editable
            }
        };

        CustomTable absensiTable = new CustomTable(model);

        // Adjust table column widths
        setTableColumnWidths(absensiTable);

        return new JScrollPane(absensiTable);
    }

    private void setTableColumnWidths(CustomTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("ABSENSI MANAGEMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private CustomPanel createDetailPanel() {
        // Panel detail data absensi dengan CustomPanel
        CustomPanel detailPanel = new CustomPanel(25);
        detailPanel.setLayout(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Buttons and date-time label for check panel
        btnCheckIn = new JButton("Check In");
        btnCheckOut = new JButton("Check Out");
        btnIjin = new JButton("Ijin");
        lblDateTime = new JLabel();
        lblCheckInTime = new JLabel("Check In Today: -");
        lblCheckOutTime = new JLabel("Check Out Today: -");

        // Set button styles
        btnCheckIn.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCheckOut.setFont(new Font("Arial", Font.PLAIN, 14));
        btnIjin.setFont(new Font("Arial", Font.PLAIN, 14));

        // Set current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        lblDateTime.setText("Today: " + formatter.format(date));
        lblDateTime.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDateTime.setForeground(Color.BLACK);

        // Add date-time label to the top left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        detailPanel.add(lblDateTime, gbc);

        // Add check-in time label above the check-in button
        gbc.gridy = 1;
        gbc.gridx = 0;
        detailPanel.add(lblCheckInTime, gbc);

        // Add check-in button to the left
        gbc.gridy = 2;
        detailPanel.add(btnCheckIn, gbc);

        // Add check-out time label above the check-out button
        gbc.gridy = 1;
        gbc.gridx = 1;
        detailPanel.add(lblCheckOutTime, gbc);

        // Add check-out button to the right
        gbc.gridy = 2;
        detailPanel.add(btnCheckOut, gbc);

        // Add ijin button to the right
        gbc.gridy = 2;
        gbc.gridx = 2;
        detailPanel.add(btnIjin, gbc);

        // Add action listeners to the buttons
        btnCheckIn.addActionListener(e -> {
            if (!ijinClickedToday && !checkInClickedToday) {
                // Show a warning dialog before proceeding
                int response = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to Check In? This action will be recorded.",
                        "Warning",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (response == JOptionPane.OK_OPTION) {
                    Date checkInDate = new Date();
                    lblCheckInTime.setText("Check In Today: " + formatter.format(checkInDate));
                    updateAbsensi(uuid, "masuk");
                    checkInClickedToday = true; // Set the flag to true after clicking "Check In"
                }
            } else if (ijinClickedToday) {
                JOptionPane.showMessageDialog(this, "You cannot check in after taking leave (Ijin) today.");
            } else {
                JOptionPane.showMessageDialog(this, "You have already checked in today.");
            }
        });

        btnCheckOut.addActionListener(e -> {
            if (!checkInClickedToday) {
                // Show a warning if trying to check out before checking in
                JOptionPane.showMessageDialog(
                        this,
                        "You must Check In before you can Check Out.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            } else if (!ijinClickedToday && !checkOutClickedToday) {
                // Show a warning dialog before proceeding
                int response = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to Check Out? This action will be recorded.",
                        "Warning",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (response == JOptionPane.OK_OPTION) {
                    Date checkOutDate = new Date();
                    lblCheckOutTime.setText("Check Out Today: " + formatter.format(checkOutDate));
                    updateAbsensi(uuid, "pulang");
                    checkOutClickedToday = true; // Set the flag to true after clicking "Check Out"
                }
            } else if (ijinClickedToday) {
                JOptionPane.showMessageDialog(this, "You cannot check out after taking leave (Ijin) today.");
            } else {
                JOptionPane.showMessageDialog(this, "You have already checked out today.");
            }
        });

        btnIjin.addActionListener(e -> {
            if (!ijinClickedToday && !checkInClickedToday && !checkOutClickedToday) {
                String keterangan = showIjinDialog();
                if (keterangan != null) {
                    Date ijinDate = new Date();
                    lblCheckInTime.setText("Check In Today: " + formatter.format(ijinDate));
                    lblCheckOutTime.setText("Check Out Today: " + formatter.format(ijinDate));
                    updateAbsensi(uuid, "ijin");
                    updateAbsensi(uuid, keterangan);
                    ijinClickedToday = true; // Set the flag to true after clicking "Ijin"
                }
            } else {
                JOptionPane.showMessageDialog(this, "You cannot take leave (Ijin) after checking in or checking out today.");
            }
        });

        return detailPanel;
    }

    private String showIjinDialog() {
        JTextField keteranganField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Keterangan:"));
        panel.add(keteranganField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Ijin Dialog", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return keteranganField.getText();
        }
        return null;
    }

    private void updateAbsensi(String uuid, String status) {
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL user_absensi(?, ?)";
        executor.executeUpdateQuery(query, new Object[]{uuid, status});
        refreshTableData(uuid);
    }

    private void refreshTableData(String uuid) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT a.id_absensi, a.id_user, u.nama_lengkap AS user_name, a.waktu_masuk, a.waktu_pulang, a.keterangan " +
                       "FROM absensi a " +
                       "JOIN user u ON a.id_user = u.id_user " +
                       "WHERE a.id_user = ? " +
                       "ORDER BY a.waktu_masuk DESC";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{uuid});

        data = new Object[][]{};
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    result.get("id_absensi"), result.get("user_name"), result.get("waktu_masuk"), result.get("waktu_pulang"), result.get("keterangan")
                };

                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;
            }

            // Update the check-in and check-out labels with the latest data
            Map<String, Object> latestEntry = results.get(0);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date waktuMasuk = (Date) latestEntry.get("waktu_masuk");
            Date waktuPulang = (Date) latestEntry.get("waktu_pulang");

            lblCheckInTime.setText("Check In Today: " + (waktuMasuk != null && isToday(waktuMasuk) ? formatter.format(waktuMasuk) : "-"));
            lblCheckOutTime.setText("Check Out Today: " + (waktuPulang != null && isToday(waktuPulang) ? formatter.format(waktuPulang) : "-"));

            // Set flags based on the latest entry
            checkInClickedToday = waktuMasuk != null && isToday(waktuMasuk);
            checkOutClickedToday = waktuPulang != null && isToday(waktuPulang);
            ijinClickedToday = "ijin".equals(latestEntry.get("keterangan")) && isToday(waktuMasuk);
        }

        model.setDataVector(data, new String[]{"ID", "Nama", "Waktu Masuk", "Waktu Pulang", "Status"});
    }

    private boolean isToday(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = formatter.format(date);
        String todayStr = formatter.format(new Date());
        return dateStr.equals(todayStr);
    }

    private void startTimer() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
            Date date = new Date();
            lblDateTime.setText("Today: " + formatter.format(date));
        });
        timer.start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Absensi().setVisible(true));
    }
}
