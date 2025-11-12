package view.panels;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import model.user.UserManager;

public class UserProfilePanel extends JPanel {
    private String currentTheme = "Blu";
    private String currentUsername = null;
    private Map<String, String> accounts = new HashMap<>();

    // Componenti per Login
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginTogglePasswordButton;
    private JButton loginButton;
    private JButton switchToRegisterButton;

    // Componenti per Registrazione
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JButton registerTogglePasswordButton;
    private JPasswordField registerConfirmPasswordField;
    private JButton registerToggleConfirmPasswordButton;
    private JButton registerButton;
    private JButton switchToLoginButton;

    // Componenti per Profilo Autenticato
    private JTextField profileUsernameField;
    private JPasswordField profilePasswordField;
    private JButton profileTogglePasswordButton;
    private JButton logoutButton;

    // ← NUOVO: Listener per il login
    private Runnable onLoginListener;

    private enum State {
        LOGIN, REGISTER, AUTHENTICATED
    }
    private State currentState = State.LOGIN;
    private boolean passwordVisible = false;

    public UserProfilePanel() {
        // ← NUOVO: Carica gli utenti salvati
        UserManager.caricaUtenti();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new CardLayout());
        setOpaque(false);

        add(createLoginPanel(), "LOGIN");
        add(createRegisterPanel(), "REGISTER");
        add(createAuthenticatedPanel(), "AUTHENTICATED");

        showLoginPanel();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        contentPanel.add(new JLabel("Username:"));
        contentPanel.add(Box.createVerticalStrut(5));
        loginUsernameField = new JTextField();
        loginUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        loginUsernameField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        contentPanel.add(loginUsernameField);

        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(new JLabel("Password:"));
        contentPanel.add(Box.createVerticalStrut(5));

        JPanel loginPasswordPanel = new JPanel(new BorderLayout(5, 0));
        loginPasswordPanel.setOpaque(false);
        loginPasswordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        loginPasswordField = new JPasswordField();
        loginPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginPasswordField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        loginPasswordPanel.add(loginPasswordField, BorderLayout.CENTER);

        loginTogglePasswordButton = createPasswordToggleButton();
        loginTogglePasswordButton.addActionListener(e -> togglePasswordField(loginPasswordField, loginTogglePasswordButton));
        loginPasswordPanel.add(loginTogglePasswordButton, BorderLayout.EAST);

        contentPanel.add(loginPasswordPanel);

        contentPanel.add(Box.createVerticalStrut(25));

        loginButton = createButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        contentPanel.add(loginButton);

        contentPanel.add(Box.createVerticalStrut(10));

        switchToRegisterButton = createButton("Registrati");
        switchToRegisterButton.addActionListener(e -> showRegisterPanel());
        contentPanel.add(switchToRegisterButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Registrazione");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        contentPanel.add(new JLabel("Username:"));
        contentPanel.add(Box.createVerticalStrut(5));
        registerUsernameField = new JTextField();
        registerUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        registerUsernameField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        contentPanel.add(registerUsernameField);

        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(new JLabel("Password:"));
        contentPanel.add(Box.createVerticalStrut(5));

        JPanel registerPasswordPanel = new JPanel(new BorderLayout(5, 0));
        registerPasswordPanel.setOpaque(false);
        registerPasswordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        registerPasswordField = new JPasswordField();
        registerPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerPasswordField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        registerPasswordPanel.add(registerPasswordField, BorderLayout.CENTER);

        registerTogglePasswordButton = createPasswordToggleButton();
        registerTogglePasswordButton.addActionListener(e -> togglePasswordField(registerPasswordField, registerTogglePasswordButton));
        registerPasswordPanel.add(registerTogglePasswordButton, BorderLayout.EAST);

        contentPanel.add(registerPasswordPanel);

        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(new JLabel("Conferma Password:"));
        contentPanel.add(Box.createVerticalStrut(5));

        JPanel confirmPasswordPanel = new JPanel(new BorderLayout(5, 0));
        confirmPasswordPanel.setOpaque(false);
        confirmPasswordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        registerConfirmPasswordField = new JPasswordField();
        registerConfirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerConfirmPasswordField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        confirmPasswordPanel.add(registerConfirmPasswordField, BorderLayout.CENTER);

        registerToggleConfirmPasswordButton = createPasswordToggleButton();
        registerToggleConfirmPasswordButton.addActionListener(e -> togglePasswordField(registerConfirmPasswordField, registerToggleConfirmPasswordButton));
        confirmPasswordPanel.add(registerToggleConfirmPasswordButton, BorderLayout.EAST);

        contentPanel.add(confirmPasswordPanel);

        contentPanel.add(Box.createVerticalStrut(25));

        registerButton = createButton("Registrati");
        registerButton.addActionListener(e -> handleRegister());
        contentPanel.add(registerButton);

        contentPanel.add(Box.createVerticalStrut(10));

        switchToLoginButton = createButton("Torna a Login");
        switchToLoginButton.addActionListener(e -> showLoginPanel());
        contentPanel.add(switchToLoginButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuthenticatedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Profilo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        contentPanel.add(new JLabel("Username:"));
        contentPanel.add(Box.createVerticalStrut(5));
        profileUsernameField = new JTextField();
        profileUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profileUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        profileUsernameField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        profileUsernameField.setEnabled(false);
        contentPanel.add(profileUsernameField);

        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(new JLabel("Password:"));
        contentPanel.add(Box.createVerticalStrut(5));

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        profilePasswordField = new JPasswordField();
        profilePasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profilePasswordField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        profilePasswordField.setEnabled(false);
        passwordPanel.add(profilePasswordField, BorderLayout.CENTER);

        profileTogglePasswordButton = createPasswordToggleButton();
        profileTogglePasswordButton.addActionListener(e -> togglePasswordField(profilePasswordField, profileTogglePasswordButton));
        passwordPanel.add(profileTogglePasswordButton, BorderLayout.EAST);

        contentPanel.add(passwordPanel);

        contentPanel.add(Box.createVerticalStrut(25));

        logoutButton = createButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        contentPanel.add(logoutButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createPasswordToggleButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                if (passwordVisible) {
                    // Occhio aperto
                    g2d.setColor(new Color(100, 100, 100));
                    g2d.fillOval(centerX - 6, centerY - 4, 12, 8);
                    g2d.setColor(new Color(50, 50, 50));
                    g2d.fillOval(centerX - 3, centerY - 2, 6, 4);
                } else {
                    // Occhio chiuso (linea)
                    g2d.setColor(new Color(100, 100, 100));
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawLine(centerX - 6, centerY, centerX + 6, centerY);
                }
            }
        };

        button.setPreferredSize(new Dimension(45, 35));
        button.setBorder(null);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        return button;
    }

    private void togglePasswordField(JPasswordField field, JButton button) {
        if (field.getEchoChar() == 0) {
            field.setEchoChar('•');
            passwordVisible = false;
        } else {
            field.setEchoChar((char) 0);
            passwordVisible = true;
        }
        button.repaint();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(null);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);

        return button;
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Riempi tutti i campi!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!UserManager.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Username o password errata!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUsername = username;
        utenteCorrente = username;
        profileUsernameField.setText(username);
        profilePasswordField.setText(password);
        showAuthenticatedPanel();

        if (onLoginListener != null) {
            onLoginListener.run();
        }

        JOptionPane.showMessageDialog(this, "Benvenuto " + username + "!", "Login Riuscito", JOptionPane.INFORMATION_MESSAGE);
    }


    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(registerConfirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Riempi tutti i campi!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Le password non corrispondono!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Usa UserManager per registrare
        if (!UserManager.registraUtente(username, password)) {
            JOptionPane.showMessageDialog(this, "Username già esistente!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Registrazione riuscita! Accedi ora.", "Successo", JOptionPane.INFORMATION_MESSAGE);
        registerUsernameField.setText("");
        registerPasswordField.setText("");
        registerConfirmPasswordField.setText("");
        showLoginPanel();
    }

    private void handleLogout() {
        currentUsername = null;
        utenteCorrente = null; // logout globale
        loginUsernameField.setText("");
        loginPasswordField.setText("");
        showLoginPanel();

        // 🔹 Svuota i preferiti quando l’utente fa logout
        SwingUtilities.invokeLater(() -> {
            Component parent = SwingUtilities.getWindowAncestor(this);
            if (parent instanceof JFrame frame) {
                Container layered = frame.getContentPane();
                for (Component c : layered.getComponents()) {
                    if (c instanceof JLayeredPane pane) {
                        for (Component sub : pane.getComponents()) {
                            if (sub instanceof FavoritesPanel favPanel) {
                                favPanel.clearFavorites();
                                System.out.println("✓ Preferiti svuotati al logout");
                            }
                        }
                    }
                }
            }
        });

        JOptionPane.showMessageDialog(this, "Logout effettuato!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }


    private void showLoginPanel() {
        currentState = State.LOGIN;
        ((CardLayout) getLayout()).show(this, "LOGIN");
    }

    private void showRegisterPanel() {
        currentState = State.REGISTER;
        ((CardLayout) getLayout()).show(this, "REGISTER");
    }

    private void showAuthenticatedPanel() {
        currentState = State.AUTHENTICATED;
        ((CardLayout) getLayout()).show(this, "AUTHENTICATED");
    }

    // ← NUOVO: Getter per l'username
    public String getCurrentUsername() {
        return currentUsername;
    }
    // 🔹 Metodo statico per ottenere l'utente attualmente loggato (comodo da altri pannelli)
    private static String utenteCorrente = null;

    public static String getCurrentUsernameStatic() {
        return utenteCorrente;
    }

    // ← NUOVO: Setter per il listener di login
    public void setOnLoginListener(Runnable listener) {
        this.onLoginListener = listener;
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

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        repaint();
    }
}
