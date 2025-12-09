package view.components;

import model.realtime.DelayInfo;
import model.realtime.DelayInfo.DelayStatus;

import javax.swing.*;
import java.awt.*;

/**
 * Componente UI che mostra un badge colorato con lo stato ritardo/puntuale/anticipo
 */
public class DelayBadge extends JLabel {

    private DelayInfo delayInfo;
    private static final int BADGE_HEIGHT = 24;
    private static final int BADGE_MIN_WIDTH = 60;

    public DelayBadge(DelayInfo delayInfo) {
        this.delayInfo = delayInfo;
        initializeUI();
    }

    private void initializeUI() {
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setHorizontalAlignment(CENTER);

        updateBadge();

        // Dimensioni
        Dimension size = new Dimension(BADGE_MIN_WIDTH, BADGE_HEIGHT);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(new Dimension(120, BADGE_HEIGHT));
    }

    /**
     * Aggiorna il badge in base allo stato del DelayInfo
     */
    public void updateBadge() {
        if (delayInfo == null) {
            setText("N/D");
            setForeground(Color.GRAY);
            return;
        }

        DelayStatus status = delayInfo.getStatus();

        switch (status) {
            case ON_TIME:
                setText("In orario");
                setForeground(new Color(46, 125, 50)); // Verde scuro
                break;

            case DELAYED:
                int delayMin = delayInfo.getDelayMinutes();
                setText("+" + delayMin + " min");

                // Gradiente rosso in base al ritardo
                if (delayMin <= 5) {
                    setForeground(new Color(251, 140, 0)); // Arancione
                } else if (delayMin <= 10) {
                    setForeground(new Color(239, 83, 80)); // Rosso chiaro
                } else {
                    setForeground(new Color(198, 40, 40)); // Rosso scuro
                }
                break;

            case EARLY:
                setText(delayInfo.getDelayMinutes() + " min");
                setForeground(new Color(2, 136, 209)); // Blu
                break;

            case SKIPPED:
                setText("Saltata");
                setForeground(new Color(117, 117, 117)); // Grigio
                break;

            case NO_DATA:
            default:
                setText("N/D");
                setForeground(new Color(158, 158, 158)); // Grigio chiaro
                break;
        }
    }

    /**
     * Aggiorna il DelayInfo e ridisegna il badge
     */
    public void setDelayInfo(DelayInfo delayInfo) {
        this.delayInfo = delayInfo;
        updateBadge();
        repaint();
    }

    public DelayInfo getDelayInfo() {
        return delayInfo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sfondo con colore in base allo stato
        Color bgColor = getBadgeBackgroundColor();
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        // Bordo
        g2d.setColor(getForeground());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2d.dispose();
        super.paintComponent(g);
    }

    /**
     * Determina il colore di sfondo in base allo stato
     */
    private Color getBadgeBackgroundColor() {
        if (delayInfo == null) {
            return new Color(245, 245, 245);
        }

        switch (delayInfo.getStatus()) {
            case ON_TIME:
                return new Color(200, 230, 201); // Verde chiaro

            case DELAYED:
                int delayMin = delayInfo.getDelayMinutes();
                if (delayMin <= 5) {
                    return new Color(255, 224, 178); // Arancione chiaro
                } else if (delayMin <= 10) {
                    return new Color(255, 205, 210); // Rosso chiaro
                } else {
                    return new Color(239, 154, 154); // Rosso più intenso
                }

            case EARLY:
                return new Color(179, 229, 252); // Blu chiaro

            case SKIPPED:
                return new Color(224, 224, 224); // Grigio

            case NO_DATA:
            default:
                return new Color(245, 245, 245); // Bianco sporco
        }
    }

    /**
     * Factory method per creare badge "No Data"
     */
    public static DelayBadge createNoDataBadge() {
        return new DelayBadge(null);
    }

    /**
     * Factory method per creare badge "Loading"
     */
    public static DelayBadge createLoadingBadge() {
        DelayBadge badge = new DelayBadge(null);
        badge.setText("...");
        badge.setForeground(Color.GRAY);
        return badge;
    }
}
