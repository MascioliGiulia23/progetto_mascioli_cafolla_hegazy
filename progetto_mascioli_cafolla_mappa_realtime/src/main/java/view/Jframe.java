package view;

import view.frames.Mappa;
import view.frames.SplashScreen;

import javax.swing.*;

public class Jframe {

    private static volatile boolean isLoading = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showSplash();

            new SwingWorker<Mappa, Void>() {
                @Override
                protected Mappa doInBackground() {
                    return new Mappa();
                }

                @Override
                protected void done() {
                    try {
                        Mappa app = get();
                        isLoading = false;
                        splash.fadeOut(() -> {
                            app.setLocationRelativeTo(null);
                            app.setVisible(true);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }.execute();
        });
    }
}
