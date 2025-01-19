package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

public class MarketAnalysis extends JFrame {
    private JComboBox<String> cryptoDropdown;
    private JTextField searchField;
    private JTable cryptoTable;
    private DefaultTableModel tableModel;

    public MarketAnalysis() {
        setTitle("Market Analysis");
        setSize(1000, 600);
        setLayout(null); // Use null layout for absolute positioning

        // Dropdown for Crypto
        JLabel cryptoLabel = new JLabel("Select Crypto:");
        cryptoLabel.setBounds(10, 10, 150, 30);
        add(cryptoLabel);

        cryptoDropdown = new JComboBox<>(new String[]{
            "Select", "bitcoin", "ethereum", "tether", "binancecoin", "usd-coin",
            "ripple", "cardano", "dogecoin", "solana", "tron", "polkadot",
            "polygon", "litecoin", "shiba-inu", "avalanche-2", "dai",
            "wrapped-bitcoin", "uniswap", "chainlink", "leo-token"
        });
        cryptoDropdown.setBounds(170, 10, 150, 30);
        cryptoDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedCrypto = (String) cryptoDropdown.getSelectedItem();
                if (!"Select".equals(selectedCrypto)) {
                    fetchCryptoData(selectedCrypto);
                }
            }
        });
        add(cryptoDropdown);

        // Search Field
        JLabel searchLabel = new JLabel("Search Crypto:");
        searchLabel.setBounds(10, 50, 150, 30);
        add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(170, 50, 150, 30);
        add(searchField);

        // Submit Button
        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(330, 50, 100, 30);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                if (!searchText.isEmpty()) {
                    fetchCryptoData(searchText);
                }
            }
        });
        add(submitButton);

        // Copy Table Button
        JButton copyTableButton = new JButton("Copy Table");
        copyTableButton.setBounds(870, 50, 100, 30);
        copyTableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyTableToClipboard();
            }
        });
        add(copyTableButton);

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.setBounds(870, 10, 100, 30);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new HomeScreen();
            }
        });
        add(backButton);

        // Table for displaying crypto data
        String[] columnNames = {"Crypto Name", "Current Price", "Resistance Level", "Resistance Level %", "Resistance Diff", "Support Level", "Support Level %", "Support Diff", "50 DMA", "100 DMA", "24h Volume", "Volume %", "Copy"};
        tableModel = new DefaultTableModel(columnNames, 0);
        cryptoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(cryptoTable);
        scrollPane.setBounds(10, 90, 960, 460);
        add(scrollPane);

        // Set custom cell renderer for percentage columns
        cryptoTable.getColumnModel().getColumn(3).setCellRenderer(new PercentageCellRenderer());
        cryptoTable.getColumnModel().getColumn(6).setCellRenderer(new PercentageCellRenderer());
        cryptoTable.getColumnModel().getColumn(11).setCellRenderer(new PercentageCellRenderer());

        // Set custom cell renderer for copy icon
        cryptoTable.getColumnModel().getColumn(12).setCellRenderer(new CopyIconCellRenderer());
        cryptoTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = cryptoTable.rowAtPoint(e.getPoint());
                int col = cryptoTable.columnAtPoint(e.getPoint());
                if (col == 12) {
                    copyRowToClipboard(row);
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fetchCryptoData(String cryptoName) {
        String uri = "https://api.coingecko.com/api/v3/coins/" + cryptoName + "/market_chart?vs_currency=cad&days=100";

        try {
            String result = makeAPICallWithRetry(uri, new ArrayList<>());
            JSONObject jsonResponse = new JSONObject(result);

            if (jsonResponse.has("prices") && jsonResponse.has("total_volumes")) {
                JSONArray prices = jsonResponse.getJSONArray("prices");
                JSONArray volumes = jsonResponse.getJSONArray("total_volumes");

                double currentPrice = prices.getJSONArray(prices.length() - 1).getDouble(1);
                double volume24h = volumes.getJSONArray(volumes.length() - 1).getDouble(1);
                double previousVolume = volumes.getJSONArray(volumes.length() - 2).getDouble(1);
                double volumePercent = ((volume24h - previousVolume) / previousVolume) * 100;

                double resistance = 0;
                double support = Double.MAX_VALUE;
                double sum50 = 0;
                double sum100 = 0;

                for (int i = 0; i < prices.length(); i++) {
                    double price = prices.getJSONArray(i).getDouble(1);
                    if (price > resistance) {
                        resistance = price;
                    }
                    if (price < support) {
                        support = price;
                    }
                    if (i >= prices.length() - 50) {
                        sum50 += price;
                    }
                    sum100 += price;
                }

                double fiftyDMA = sum50 / 50;
                double hundredDMA = sum100 / 100;

                double resistancePercent = ((resistance - currentPrice) / currentPrice) * 100;
                double supportPercent = ((currentPrice - support) / currentPrice) * 100;
                double resistanceDiff = resistance - currentPrice;
                double supportDiff = support - currentPrice;

                tableModel.addRow(new Object[]{
                        cryptoName,
                        formatNumber(currentPrice),
                        formatNumber(resistance),
                        String.format("%+.2f%%", resistancePercent),
                        formatNumber(resistanceDiff),
                        formatNumber(support),
                        String.format("%+.2f%%", -supportPercent),
                        formatNumber(supportDiff),
                        formatNumber(fiftyDMA),
                        formatNumber(hundredDMA),
                        formatNumber(volume24h),
                        String.format("%+.2f%%", volumePercent),
                        "Copy"
                });
            } else {
                showErrorDialog("Data not found for " + cryptoName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Error fetching data for " + cryptoName + ": " + ex.getMessage());
        }
    }

    private String makeAPICallWithRetry(String uri, List<NameValuePair> parameters) throws Exception {
        int maxRetries = 5;
        int retryCount = 0;
        int waitTime = 3000; // Initial wait time in milliseconds

        while (retryCount < maxRetries) {
            try {
                return JavaExample.makeAPICall(uri, parameters);
            } catch (Exception ex) {
                if (ex.getMessage().contains("429")) {
                    retryCount++;
                    Thread.sleep(waitTime);
                    waitTime *= 2; // Exponential backoff
                } else {
                    throw ex;
                }
            }
        }
        throw new Exception("Max retries reached");
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatNumber(double number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000);
        } else {
            return String.format("%.2f", number);
        }
    }

    private void copyRowToClipboard(int row) {
        StringBuilder rowData = new StringBuilder();
        for (int col = 0; col < tableModel.getColumnCount() - 1; col++) {
            rowData.append(tableModel.getColumnName(col)).append(": ").append(tableModel.getValueAt(row, col)).append(", ");
        }
        StringSelection stringSelection = new StringSelection(rowData.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

 private void copyTableToClipboard() {
    StringBuilder tableData = new StringBuilder();
    for (int col = 0; col < tableModel.getColumnCount() - 1; col++) {
        tableData.append(tableModel.getColumnName(col)).append(", ");
    }
    tableData.append("\n");
    for (int row = 0; row < tableModel.getRowCount(); row++) {
        for (int col = 0; col < tableModel.getColumnCount() - 1; col++) {
            tableData.append(tableModel.getColumnName(col)).append(": ").append(tableModel.getValueAt(row, col)).append(", ");
        }
        tableData.append("\n");
    }
    StringSelection stringSelection = new StringSelection(tableData.toString());
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
}

    // Custom cell renderer for percentage columns
    class PercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && value.toString().contains("%")) {
                double percentage = Double.parseDouble(value.toString().replace("%", ""));
                if (percentage > 0) {
                    cell.setForeground(new Color(0, 100, 0)); // Dark Green
                } else {
                    cell.setForeground(new Color(139, 0, 0)); // Dark Red
                }
            }
            return cell;
        }
    }

    // Custom cell renderer for copy icon
    class CopyIconCellRenderer extends JButton implements TableCellRenderer {
        public CopyIconCellRenderer() {
            try {
                ImageIcon originalIcon = new ImageIcon(getClass().getClassLoader().getResource("icons/CopyIcon.png"));
                Image scaledImage = originalIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                System.err.println("Icon not found: " + e.getMessage());
            }
            setBorderPainted(false);
            setContentAreaFilled(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    public static void main(String[] args) {
        new MarketAnalysis();
    }
}