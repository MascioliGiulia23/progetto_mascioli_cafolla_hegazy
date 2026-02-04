package view.panels.search;

import javax.swing.*;
import java.awt.*;

public class SearchResultsView extends JPanel {

    private JPanel headerPanel;
    private JLabel titleLabel;
    private JButton closeButton;

    private JPanel resultsContainer;
    private JScrollPane scrollPane;

    public SearchResultsView() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // HEADER
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 3, 10));

        titleLabel = new JLabel("Risultati");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Bottone X (stile identico a FavoritesPanel)
        closeButton = new JButton("x");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.DARK_GRAY);
            }
        });

        headerPanel.add(closeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // CONTAINER RISULTATI
        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);

        scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================
    // API PUBBLICA (per SearchResultsPanel)
    // =========================

    public JPanel getResultsContainer() {
        return resultsContainer;
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
