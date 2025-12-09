package view.frames;

import view.panels.SettingsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Minimal splash screen aligned to the app theme, with a pulsing marker
 * animation and fade-out transition.
 */
public class SplashScreen extends JWindow {
    private static final int WIDTH = 420;
    private static final int HEIGHT = 280;

    private final Timer pulseTimer;
    private final JPanel pulsePanel;
    private float pulsePhase = 0f;
    private boolean fading = false;

    public SplashScreen() {
        Color theme = SettingsPanel.getThemeColor(SettingsPanel.COLOR_BLU);
        Color light = new Color(
                Math.min(theme.getRed() + 80, 255),
                Math.min(theme.getGreen() + 80, 255),
                Math.min(theme.getBlue() + 80, 255)
        );

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        JPanel container = new JPanel();
        container.setBackground(light);
        container.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        container.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Roma Bus Tracker", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 40, 40));
        container.add(title, BorderLayout.NORTH);

        pulsePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                float normalized = (float) ((Math.sin(pulsePhase) + 1) / 2); // 0..1
                int base = Math.min(w, h) / 5;
                int radius = base + (int) (normalized * (base * 0.6));

                Color ring = theme;
                Color core = ring.darker();

                int cx = w / 2;
                int cy = h / 2;

                // outer ring with alpha
                g2.setColor(new Color(ring.getRed(), ring.getGreen(), ring.getBlue(), 90));
                int ringSize = (int) (radius * 1.8);
                g2.fillOval(cx - ringSize / 2, cy - ringSize / 2, ringSize, ringSize);

                // core
                g2.setColor(core);
                g2.fillOval(cx - radius / 2, cy - radius / 2, radius, radius);

                g2.dispose();
            }
        };
        pulsePanel.setOpaque(false);
        container.add(pulsePanel, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("Caricamento dati e mappe...", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(70, 70, 70));
        container.add(subtitle, BorderLayout.SOUTH);

        setContentPane(container);

        pulseTimer = new Timer(28, e -> {
            pulsePhase += 0.12f;
            pulsePanel.repaint();
        });
    }

    public void showSplash() {
        setOpacity(1f);
        setVisible(true);
        pulseTimer.start();
    }

    public void fadeOut(Runnable onComplete) {
        if (fading) return;
        fading = true;
        Timer fadeTimer = new Timer(18, null);
        fadeTimer.addActionListener(e -> {
            float op = getOpacity() - 0.05f;
            if (op <= 0.05f) {
                fadeTimer.stop();
                pulseTimer.stop();
                setVisible(false);
                dispose();
                if (onComplete != null) onComplete.run();
            } else {
                setOpacity(op);
            }
        });
        fadeTimer.start();
    }
}


