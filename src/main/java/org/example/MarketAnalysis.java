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
    private JComboBox<String> groupDropdown;
    private JTextField searchField;
    private JTable cryptoTable;
    private DefaultTableModel tableModel;

    private static final String[] GROUP_1 = {"bitcoin", "ethereum", "binancecoin", "ripple", "cardano"};
    private static final String[] GROUP_2 = {"dogecoin", "solana", "tron", "polkadot", "polygon"};
    private static final String[] GROUP_3 = {"litecoin", "jupiter", "avalanche-2", "dai", "wrapped-bitcoin"};

    public MarketAnalysis() {
        setTitle("Market Analysis");
        setSize(1000, 600);
        setLayout(null); // Use null layout for absolute positioning

        // Dropdown for Crypto
        JLabel cryptoLabel = new JLabel("Select Crypto:");
        cryptoLabel.setBounds(10, 10, 150, 30);
        add(cryptoLabel);

        cryptoDropdown = new JComboBox<>(new String[]{
            "Select", "bitcoin", "ethereum", "solana", "jupiter", "sui",
            "ripple", "cardano", "dogecoin","tron", "polkadot",
            "polygon", "litecoin", "shiba-inu", "avalanche-2", "dai",
            "binancecoin", "uniswap", "chainlink", "leo-token"
        });
        cryptoDropdown.setBounds(170, 10, 150, 30);
        cryptoDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedCrypto = (String) cryptoDropdown.getSelectedItem();
                if (!"Select".equals(selectedCrypto)) {
                    fetchCryptoData(new String[]{selectedCrypto});
                }
            }
        });
        add(cryptoDropdown);

        // Dropdown for Group
        JLabel groupLabel = new JLabel("Select Group:");
        groupLabel.setBounds(330, 10, 150, 30);
        add(groupLabel);

        groupDropdown = new JComboBox<>(new String[]{"Select", "Group 1", "Group 2", "Group 3"});
        groupDropdown.setBounds(490, 10, 150, 30);
        groupDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedGroup = (String) groupDropdown.getSelectedItem();
                switch (selectedGroup) {
                    case "Group 1":
                        fetchCryptoData(GROUP_1);
                        break;
                    case "Group 2":
                        fetchCryptoData(GROUP_2);
                        break;
                    case "Group 3":
                        fetchCryptoData(GROUP_3);
                        break;
                }
            }
        });
        add(groupDropdown);

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
                    fetchCryptoData(new String[]{searchText.toLowerCase().replace(" ", "-")});
                }
            }
        });
        add(submitButton);

        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(760, 50, 100, 30);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0); // Clear the table
            }
        });
        add(resetButton);


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
              HomeScreen.getInstance();
            }
        });
        add(backButton);

        // Table for displaying crypto data
        String[] columnNames = {"Crypto Name", "Current Price", "Resistance Level", "Resistance Level %", "Resistance Diff", "Support Level", "Support Level %", "Support Diff", "50 DMA", "100 DMA", "24h Volume", "Volume %", "RSI", "Copy"};
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
        cryptoTable.getColumnModel().getColumn(13).setCellRenderer(new CopyIconCellRenderer());
        cryptoTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = cryptoTable.rowAtPoint(e.getPoint());
                int col = cryptoTable.columnAtPoint(e.getPoint());
                if (col == 13) {
                    copyRowToClipboard(row);
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fetchCryptoData(String[] cryptoNames) {
        for (String cryptoName : cryptoNames) {
            String uri = "https://api.coingecko.com/api/v3/coins/" + cryptoName + "/market_chart?vs_currency=cad&days=1"; // Fetch 100 days for 100 DMA
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

                    // ** Resistance & Support Calculation **
                    double resistance = 0;
                    double support = Double.MAX_VALUE;
                    List<Double> closingPrices = new ArrayList<>();

                    for (int i = 0; i < prices.length(); i++) {
                        double price = prices.getJSONArray(i).getDouble(1);
                        closingPrices.add(price);
                        resistance = Math.max(resistance, price);
                        support = Math.min(support, price);
                    }

                    // ** Moving Average Calculations (50 DMA & 100 DMA) **
                    double dma50 = calculateMovingAverage(closingPrices, 50);
                    double dma100 = calculateMovingAverage(closingPrices, 100);

                    // ** RSI Calculation (14-day period) **
                    double rsi = calculateRSI(closingPrices, 14);

                    // ** Percentage & Difference Calculation **
                    double resistancePercent = ((resistance - currentPrice) / currentPrice) * 100;
                    double supportPercent = ((currentPrice - support) / currentPrice) * 100;
                    double resistanceDiff = resistance - currentPrice;
                    double supportDiff = support - currentPrice;

                    // ** Adding Data to Table Model **
                    tableModel.addRow(new Object[]{
                            cryptoName,
                            formatNumber(currentPrice),
                            formatNumber(resistance),
                            String.format("%+.2f%%", resistancePercent),
                            formatNumber(resistanceDiff),
                            formatNumber(support),
                            String.format("%+.2f%%", -supportPercent),
                            formatNumber(supportDiff),
                            formatNumber(dma50),  // 50 DMA
                            formatNumber(dma100), // 100 DMA
                            formatNumber(volume24h),
                            String.format("%+.2f%%", volumePercent),
                            formatNumber(rsi), // RSI
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
    }

    // Calculate Moving Average (DMA)
    private double calculateMovingAverage(List<Double> prices, int period) {
        if (prices.size() < period) return Double.NaN;
        double sum = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    // Calculate RSI (Relative Strength Index)
    private double calculateRSI(List<Double> prices, int period) {
        if (prices.size() < period + 1) return Double.NaN;

        double gain = 0, loss = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0) return 100; // RSI is 100 if no losses

        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
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