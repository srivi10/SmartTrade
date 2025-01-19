package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


public class StopTrail extends JFrame {
    private JComboBox<String> cryptoDropdown;
    private JTextField priceField, orderSizeField, trailingPercentageField, setTrailingField, resistanceField, supportField;
    private JTextField resistancePercentField, supportPercentField, resistanceDiffField, supportDiffField;
    private JRadioButton lastRadioButton;
    private JLabel resultLabel;

    public StopTrail() {
        setTitle("Crypto Trailing Stop Calculator");
        setSize(600, 500);
        setLayout(null); // Use null layout for absolute positioning

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 10, 80, 30);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the current window
                new HomeScreen(); // Open the HomeScreen
            }
        });
        add(backButton);

        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(100, 10, 80, 30);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        });
        add(resetButton);

        // Dropdown for Cryptocurrency
        JLabel cryptoLabel = new JLabel("Cryptocurrency:");
        cryptoLabel.setBounds(10, 50, 120, 30);
        add(cryptoLabel);

        cryptoDropdown = new JComboBox<>(new String[] {"Select", "bitcoin", "ethereum", "tether", "binancecoin", "usd-coin", "ripple", "cardano", "dogecoin", "solana", "tron", "polkadot", "polygon", "litecoin", "shiba-inu", "avalanche-2", "dai", "wrapped-bitcoin", "uniswap", "chainlink", "leo-token"});
        cryptoDropdown.setBounds(140, 50, 150, 30);
        cryptoDropdown.setSelectedIndex(0);
        cryptoDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cryptoDropdown.getSelectedIndex() != 0) {
                    fetchCurrentPrice((String) cryptoDropdown.getSelectedItem());
                    fetchResistanceSupportLevels((String) cryptoDropdown.getSelectedItem());
                }
            }
        });
        add(cryptoDropdown);

        // TextField for Current Price (Auto-filled via API)
        JLabel priceLabel = new JLabel("Current Price:");
        priceLabel.setBounds(10, 90, 120, 30);
        add(priceLabel);

        priceField = new JTextField();
        priceField.setBounds(140, 90, 150, 30);
        add(priceField);

        // TextField for Order Size
        JLabel orderSizeLabel = new JLabel("Order Size:");
        orderSizeLabel.setBounds(10, 130, 120, 30);
        add(orderSizeLabel);

        orderSizeField = new JTextField();
        orderSizeField.setBounds(140, 130, 150, 30);
        add(orderSizeField);

        // TextField for Trailing Percentage
        JLabel trailingLabel = new JLabel("Trailing Percentage:");
        trailingLabel.setBounds(10, 170, 120, 30);
        add(trailingLabel);

        trailingPercentageField = new JTextField("5");
        trailingPercentageField.setBounds(140, 170, 150, 30);
        add(trailingPercentageField);

        // Radio Button for Peg Price Type
        JLabel pegTypeLabel = new JLabel("Peg Price Type:");
        pegTypeLabel.setBounds(10, 210, 120, 30);
        add(pegTypeLabel);

        lastRadioButton = new JRadioButton("Last", true);
        lastRadioButton.setBounds(140, 210, 150, 30);
        add(lastRadioButton);

        // TextField for Set Trailing (Auto-calculated)
        JLabel setTrailingLabel = new JLabel("Set Trailing:");
        setTrailingLabel.setBounds(10, 250, 120, 30);
        add(setTrailingLabel);

        setTrailingField = new JTextField();
        setTrailingField.setBounds(140, 250, 150, 30);
        setTrailingField.setEditable(false);
        add(setTrailingField);

        // TextField for Resistance Level (Auto-filled via API)
        JLabel resistanceLabel = new JLabel("Resistance Level:");
        resistanceLabel.setBounds(10, 290, 120, 30);
        add(resistanceLabel);

        resistanceField = new JTextField();
        resistanceField.setBounds(140, 290, 150, 30);
        resistanceField.setEditable(false);
        add(resistanceField);

        // TextField for Resistance Percentage (Auto-calculated)
        resistancePercentField = new JTextField();
        resistancePercentField.setBounds(300, 290, 150, 30);
        resistancePercentField.setEditable(false);
        add(resistancePercentField);

        // TextField for Resistance Difference (Auto-calculated)
        resistanceDiffField = new JTextField();
        resistanceDiffField.setBounds(460, 290, 150, 30);
        resistanceDiffField.setEditable(false);
        add(resistanceDiffField);

        // TextField for Support Level (Auto-filled via API)
        JLabel supportLabel = new JLabel("Support Level:");
        supportLabel.setBounds(10, 330, 120, 30);
        add(supportLabel);

        supportField = new JTextField();
        supportField.setBounds(140, 330, 150, 30);
        supportField.setEditable(false);
        add(supportField);

        // TextField for Support Percentage (Auto-calculated)
        supportPercentField = new JTextField();
        supportPercentField.setBounds(300, 330, 150, 30);
        supportPercentField.setEditable(false);
        add(supportPercentField);

        // TextField for Support Difference (Auto-calculated)
        supportDiffField = new JTextField();
        supportDiffField.setBounds(460, 330, 150, 30);
        supportDiffField.setEditable(false);
        add(supportDiffField);

        // Calculate Button
        JButton calculateButton = new JButton("Calculate");
        calculateButton.setBounds(10, 370, 100, 30);
        calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculateTrailingStop();
            }
        });
        add(calculateButton);

        // Result Label
        JLabel resultTextLabel = new JLabel("Result:");
        resultTextLabel.setBounds(10, 410, 120, 30);
        add(resultTextLabel);

        resultLabel = new JLabel("");
        resultLabel.setBounds(140, 410, 300, 30);
        add(resultLabel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Fetch current price from API
    private void fetchCurrentPrice(String cryptoName) {
        String uri = "https://api.coingecko.com/api/v3/simple/price";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("ids", cryptoName));
        parameters.add(new BasicNameValuePair("vs_currencies", "cad"));

        try {
            String result = JavaExample.makeAPICall(uri, parameters);
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.has(cryptoName)) {
                double price = jsonResponse.getJSONObject(cryptoName).getDouble("cad");
                priceField.setText(String.valueOf(price));
            } else {
                showErrorDialog("Cryptocurrency not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Error fetching current price: " + ex.getMessage());
        }
    }

    // Fetch resistance and support levels from API
    private void fetchResistanceSupportLevels(String cryptoName) {
        String uri = "https://api.coingecko.com/api/v3/coins/" + cryptoName + "/market_chart";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("vs_currency", "cad"));
        parameters.add(new BasicNameValuePair("days", "1"));

        try {
            String result = JavaExample.makeAPICall(uri, parameters);
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.has("prices")) {
                JSONArray prices = jsonResponse.getJSONArray("prices");
                double resistance = 0;
                double support = Double.MAX_VALUE;
                for (int i = 0; i < prices.length(); i++) {
                    double price = prices.getJSONArray(i).getDouble(1);
                    if (price > resistance) {
                        resistance = price;
                    }
                    if (price < support) {
                        support = price;
                    }
                }
                resistanceField.setText(String.format("%.2f", resistance));
                supportField.setText(String.format("%.2f", support));
                calculateResistanceSupportPercentages();
            } else {
                showErrorDialog("Resistance and support levels not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Error fetching resistance and support levels: " + ex.getMessage());
        }
    }

    private void calculateResistanceSupportPercentages() {
        try {
            double currentPrice = Double.parseDouble(priceField.getText());
            double resistance = Double.parseDouble(resistanceField.getText());
            double support = Double.parseDouble(supportField.getText());

            double resistancePercent = ((resistance - currentPrice) / currentPrice) * 100;
            double supportPercent = ((currentPrice - support) / currentPrice) * 100;

            resistancePercentField.setText(String.format("%+.2f%%", resistancePercent));
            supportPercentField.setText(String.format("%+.2f%%", -supportPercent));

            double resistanceDiff = resistance - currentPrice;
            double supportDiff = support - currentPrice;

            resistanceDiffField.setText(String.format("%.2f", resistanceDiff));
            supportDiffField.setText(String.format("%.2f", supportDiff));

            if (resistancePercent > 0) {
                resistancePercentField.setForeground(new Color(0, 100, 0)); // Dark Green
            } else {
                resistancePercentField.setForeground(Color.RED);
            }

            if (supportPercent > 0) {
                supportPercentField.setForeground(Color.RED);
            } else {
                supportPercentField.setForeground(new Color(0, 100, 0)); // Dark Green
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Error calculating resistance and support percentages: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Perform calculations
    private void calculateTrailingStop() {
        try {
            String priceText = priceField.getText();
            String orderSizeText = orderSizeField.getText();
            String trailingPercentageText = trailingPercentageField.getText();

            if (priceText.isEmpty() || orderSizeText.isEmpty() || trailingPercentageText.isEmpty()) {
                resultLabel.setText("Please fill in all fields.");
                return;
            }

            double currentPrice = Double.parseDouble(priceText);
            double orderSize = Double.parseDouble(orderSizeText);
            double trailingPercentage = Double.parseDouble(trailingPercentageText);

            double trailingAmount = currentPrice * (trailingPercentage / 100);
            double stopPrice = currentPrice - trailingAmount;
            double orderTotal = currentPrice * orderSize;
            double fees = orderTotal * 0.0025; // Assuming 0.25% trading fee
            double netReceived = orderTotal - fees;
            double setTrailing = currentPrice - stopPrice;

            setTrailingField.setText(String.format("%.4f", setTrailing));
            resultLabel.setText(String.format("Trailing Stop Price: %.4f, Net Received: %.4f CAD", stopPrice, netReceived));
        } catch (NumberFormatException e) {
            resultLabel.setText("Invalid input. Please enter valid numbers.");
        }
    }

    // Reset all fields except Trailing Percentage
    private void resetFields() {
        cryptoDropdown.setSelectedIndex(0);
        priceField.setText("");
        orderSizeField.setText("");
        setTrailingField.setText("");
        resistanceField.setText("");
        supportField.setText("");
        resistancePercentField.setText("");
        supportPercentField.setText("");
        resistanceDiffField.setText("");
        supportDiffField.setText("");
        resultLabel.setText("");
    }

    public static void main(String[] args) {
        new StopTrail();
    }
}