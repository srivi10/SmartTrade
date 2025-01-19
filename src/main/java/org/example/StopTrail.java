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
    private JRadioButton lastRadioButton;
    private JLabel resultLabel;

    public StopTrail() {
        setTitle("Crypto Trailing Stop Calculator");
        setSize(400, 500);
        setLayout(new GridLayout(12, 2));

        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        });
        add(resetButton);
        add(new JLabel()); // Empty space

        // Dropdown for Cryptocurrency
        JLabel cryptoLabel = new JLabel("Cryptocurrency:");
        cryptoDropdown = new JComboBox<>(new String[] {"Select", "bitcoin", "ethereum", "tether", "binancecoin", "usd-coin", "ripple", "cardano", "dogecoin", "solana", "tron", "polkadot", "polygon", "litecoin", "shiba-inu", "avalanche-2", "dai", "wrapped-bitcoin", "uniswap", "chainlink", "leo-token"});
        cryptoDropdown.setSelectedIndex(0);
        cryptoDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cryptoDropdown.getSelectedIndex() != 0) {
                    fetchCurrentPrice((String) cryptoDropdown.getSelectedItem());
                    fetchResistanceSupportLevels((String) cryptoDropdown.getSelectedItem());
                }
            }
        });
        add(cryptoLabel);
        add(cryptoDropdown);

        // TextField for Current Price (Auto-filled via API)
        JLabel priceLabel = new JLabel("Current Price:");
        priceField = new JTextField();
        add(priceLabel);
        add(priceField);

        // TextField for Order Size
        JLabel orderSizeLabel = new JLabel("Order Size:");
        orderSizeField = new JTextField();
        add(orderSizeLabel);
        add(orderSizeField);

        // TextField for Trailing Percentage
        JLabel trailingLabel = new JLabel("Trailing Percentage:");
        trailingPercentageField = new JTextField("5");
        add(trailingLabel);
        add(trailingPercentageField);

        // Radio Button for Peg Price Type
        JLabel pegTypeLabel = new JLabel("Peg Price Type:");
        lastRadioButton = new JRadioButton("Last", true);
        add(pegTypeLabel);
        add(lastRadioButton);

        // TextField for Set Trailing (Auto-calculated)
        JLabel setTrailingLabel = new JLabel("Set Trailing:");
        setTrailingField = new JTextField();
        setTrailingField.setEditable(false);
        add(setTrailingLabel);
        add(setTrailingField);

        // TextField for Resistance Level (Auto-filled via API)
        JLabel resistanceLabel = new JLabel("Resistance Level:");
        resistanceField = new JTextField();
        resistanceField.setEditable(false);
        add(resistanceLabel);
        add(resistanceField);

        // TextField for Support Level (Auto-filled via API)
        JLabel supportLabel = new JLabel("Support Level:");
        supportField = new JTextField();
        supportField.setEditable(false);
        add(supportLabel);
        add(supportField);

        // Calculate Button
        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculateTrailingStop();
            }
        });
        add(new JLabel()); // Empty space
        add(calculateButton);

        // Result Label
        JLabel resultTextLabel = new JLabel("Result:");
        resultLabel = new JLabel("");
        add(resultTextLabel);
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
                resistanceField.setText(String.valueOf(resistance));
                supportField.setText(String.valueOf(support));
            } else {
                showErrorDialog("Resistance and support levels not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Error fetching resistance and support levels: " + ex.getMessage());
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
        resultLabel.setText("");
    }

    public static void main(String[] args) {
        new StopTrail();
    }
}