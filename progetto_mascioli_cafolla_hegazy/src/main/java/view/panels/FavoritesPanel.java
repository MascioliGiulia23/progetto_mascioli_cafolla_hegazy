package view.panels;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import model.user.UserManager;
import model.user.Favorite;

public class FavoritesPanel extends JPanel {
    private JPanel favoritesContainer;
    private JScrollPane scrollPane;
    private List<FavoriteItem> favorites;
    private String currentTheme = "Blu";

    private java.util.function.Consumer<Favorite> onFavoriteClickListener;

    // AGGIUNTE (serve per i test)
    private java.util.function.Supplier<String> usernameSupplier = // serve per i test
            () -> UserProfilePanel.getCurrentUsernameStatic(); // serve per i test

    private java.util.function.Function<String, java.util.List<Favorite>> favoritesProvider = // serve per i test
            (u) -> UserManager.ottieniPreferiti(u); // serve per i test

    private java.util.function.BiConsumer<String, Favorite> favoriteRemover = // serve per i test
            (u, f) -> UserManager.rimuoviPreferito(u, f); // serve per i test


    public FavoritesPanel() {
        favorites = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("I Miei Preferiti");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        favoritesContainer = new JPanel();
        favoritesContainer.setLayout(new BoxLayout(favoritesContainer, BoxLayout.Y_AXIS));
        favoritesContainer.setOpaque(false);
        favoritesContainer.setAlignmentY(Component.TOP_ALIGNMENT);

        scrollPane = new JScrollPane(favoritesContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        Color themeColor = SettingsPanel.getThemeColor(currentTheme);
        Color lightColor = new Color(
                Math.min(themeColor.getRed() + 150, 255),
                Math.min(themeColor.getGreen() + 150, 255),
                Math.min(themeColor.getBlue() + 150, 255)
        );
        g2d.setColor(lightColor);
        g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(themeColor);
        g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);
    }

    // Carica i preferiti in base all'utente attualmente loggato
    public void caricaPreferiti() {
        clearFavorites();

        //  (serve per i test): usa supplier invece di chiamata statica diretta

        String username = usernameSupplier.get(); // serve per i test
        if (username == null || username.isEmpty()) {
            addEmptyMessageNotLogged();
            return;
        }


        // (serve per i test): usa provider invece di chiamata statica diretta

        java.util.List<Favorite> preferiti = favoritesProvider.apply(username); // serve per i test


        if (preferiti == null || preferiti.isEmpty()) {
            addEmptyMessageNoFavorites();
            return;
        }

        for (Favorite fav : preferiti) {
            addFavoriteFromModel(fav);
        }
        favoritesContainer.add(Box.createVerticalGlue());
    }

    //  Metodo setter per il listener
    public void setOnFavoriteClickListener(java.util.function.Consumer<Favorite> listener) {
        this.onFavoriteClickListener = listener;
    }

    //  Aggiunge un preferito dal model
    private void addFavoriteFromModel(Favorite fav) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);

        // margini molto più piccoli
        itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
        // altezza massima contenuta (riga compatta)
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel nameLabel = new JLabel(fav.getNome());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel tipoLabel = new JLabel("(" + fav.getTipo() + ")");
        tipoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tipoLabel.setForeground(new Color(100, 100, 100));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel);
        infoPanel.add(tipoLabel);

        // Bottone rimozione compatto
        JButton removeBtn = new JButton("X");
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        removeBtn.setForeground(Color.DARK_GRAY);
        removeBtn.setFocusPainted(false);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        removeBtn.setPreferredSize(new Dimension(16, 16));
        removeBtn.setMaximumSize(new Dimension(16, 16));
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        removeBtn.addActionListener(e -> {

            //(serve per i test): usa supplier + remover invece di static diretti

            String currentUser = usernameSupplier.get(); // serve per i test
            if (currentUser != null) {
                favoriteRemover.accept(currentUser, fav); // serve per i test
                caricaPreferiti(); // aggiorna lista dopo rimozione
            }
        });

        itemPanel.add(infoPanel, BorderLayout.CENTER);
        itemPanel.add(removeBtn, BorderLayout.EAST);

        // Listener di click sul preferito
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Se clicchi sul bottone X, non fare nulla
                if (removeBtn.getBounds().contains(e.getPoint())) {
                    return;
                }

                // Chiama il listener
                if (onFavoriteClickListener != null) {
                    onFavoriteClickListener.accept(fav);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(new Color(220, 220, 220));
                itemPanel.setOpaque(true);
                itemPanel.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setOpaque(false);
                itemPanel.repaint();
            }
        });

        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        favoritesContainer.add(itemPanel);
        favoritesContainer.revalidate();
        favoritesContainer.repaint();
    }

    // Utente NON loggato
    private void addEmptyMessageNotLogged() {
        JLabel emptyLabel = new JLabel("Effettua il login per accedere alla sezione Preferiti");
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        emptyLabel.setForeground(new Color(120, 120, 120));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        favoritesContainer.add(Box.createVerticalGlue());
        favoritesContainer.add(emptyLabel);
        favoritesContainer.add(Box.createVerticalGlue());
        favoritesContainer.revalidate();
        favoritesContainer.repaint();
    }

    // Utente loggato ma senza preferiti
    private void addEmptyMessageNoFavorites() {
        JLabel emptyLabel = new JLabel("Nessun preferito salvato. Aggiungi fermate o linee!");
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        emptyLabel.setForeground(new Color(120, 120, 120));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        favoritesContainer.add(Box.createVerticalGlue());
        favoritesContainer.add(emptyLabel);
        favoritesContainer.add(Box.createVerticalGlue());
        favoritesContainer.revalidate();
        favoritesContainer.repaint();
    }

    public void addFavorite(String name, String details) {
        FavoriteItem item = new FavoriteItem(name, details, currentTheme);
        favorites.add(item);
        favoritesContainer.add(item);
        favoritesContainer.revalidate();
        favoritesContainer.repaint();
    }

    public void clearFavorites() {
        favoritesContainer.removeAll();
        favorites.clear();
        favoritesContainer.revalidate();
        favoritesContainer.repaint();
    }

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        for (FavoriteItem item : favorites) {
            item.updateTheme(theme);
        }
        repaint();
    }

    // SETTER DI DIPENDENZE (serve per i test)
    void setUsernameSupplierForTest(java.util.function.Supplier<String> supplier) { // serve per i test
        this.usernameSupplier = supplier; // serve per i test
    }

    void setFavoritesProviderForTest(java.util.function.Function<String, java.util.List<Favorite>> provider) { // serve per i test
        this.favoritesProvider = provider; // serve per i test
    }

    void setFavoriteRemoverForTest(java.util.function.BiConsumer<String, Favorite> remover) { // serve per i test
        this.favoriteRemover = remover; // serve per i test
    }

    JPanel getFavoritesContainerForTest() { // serve per i test
        return favoritesContainer; // serve per i test
    }

    int getModelFavoritesCountForTest() { // serve per i test
        return favorites.size(); // serve per i test
    }

    public static class FavoriteItem extends JPanel {
        private String name;
        private String details;
        private String currentTheme;

        public FavoriteItem(String name, String details, String theme) {
            this.name = name;
            this.details = details;
            this.currentTheme = theme;
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JLabel iconLabel = new JLabel("");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            add(iconLabel, BorderLayout.WEST);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLabel.setForeground(new Color(50, 50, 50));
            infoPanel.add(nameLabel);

            JLabel detailsLabel = new JLabel(details);
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            detailsLabel.setForeground(new Color(120, 120, 120));
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(detailsLabel);

            add(infoPanel, BorderLayout.CENTER);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(new Color(245, 245, 245));
                    setOpaque(true);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setOpaque(false);
                }
            });
        }

        public void updateTheme(String theme) {
            this.currentTheme = theme;
        }
    }
}
