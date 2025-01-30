package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeScreen extends JFrame {
    private static HomeScreen instance;

    private HomeScreen() {
        setTitle("Home Screen");
        setSize(400, 200);
        setLayout(null); // Use null layout for absolute positioning

        JButton stopTrailButton = new JButton("Stop Trail");
        stopTrailButton.setBounds(50, 50, 120, 30); // Set position and size
        stopTrailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new StopTrail();
            }
        });
        add(stopTrailButton);

        JButton marketAnalysisButton = new JButton("Market Analysis");
        marketAnalysisButton.setBounds(200, 50, 150, 30); // Set position and size
        marketAnalysisButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Placeholder for Market Analysis screen
                new MarketAnalysis();
             //   JOptionPane.showMessageDialog(null, "Market Analysis screen will be implemented later.");
            }
        });
        add(marketAnalysisButton);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static HomeScreen getInstance() {
        if (instance == null) {
            instance = new HomeScreen();
        }
        return instance;
    }

    public static void main(String[] args) {
        HomeScreen.getInstance();
    }
}