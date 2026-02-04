package view.panels.search;

import model.gtfs.Fermate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchItem extends JPanel {

    private String title;
    private String description;
    private String icon;
    private String currentTheme;
    private Fermate fermata;
    private OnItemClickListener onItemClickListener;
    private boolean isHovered = false;

    public SearchItem(String title, String description, String icon, String theme, Fermate fermata) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.currentTheme = theme;
        this.fermata = fermata;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(50, 50, 50));
        infoPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(new Color(120, 120, 120));
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(descriptionLabel);

        add(infoPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setBackground(new Color(245, 245, 245));
                setOpaque(true);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setOpaque(false);
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onItemClickListener != null) onItemClickListener.onClick();
            }
        });
    }

    public void updateTheme(String theme) {
        this.currentTheme = theme;
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick();
    }

    void clickForTest() { // serve per i test
        if (onItemClickListener != null) onItemClickListener.onClick(); // serve per i test
    }
}
