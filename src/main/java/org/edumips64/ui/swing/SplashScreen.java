package org.edumips64.ui.swing;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class SplashScreen extends JDialog {

    private static final int SPLASH_WIDTH = 500;
    private static final int SPLASH_HEIGHT = 300;

    public SplashScreen() {
        super((JFrame) null);
        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/images/splash.png"));
        setSplashProperties(imageIcon);
    }

    private void setSplashProperties(ImageIcon img) {
        this.setModal(false);
        this.setUndecorated(true);
        this.add(new JLabel(img));
        this.setPreferredSize(new Dimension(SPLASH_WIDTH, SPLASH_HEIGHT));
        centerSplash();
    }

    public void centerSplash() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle screen = gc.getBounds();
        int y = (screen.height - SPLASH_HEIGHT) / 2;
        int x = (screen.width - SPLASH_WIDTH) / 2;
        this.setLocation(x, y);
    }

    public void showSplash() {
        this.pack();
        this.setVisible(true);
    }

    public void closeSplash() {
        this.setVisible(false);
        this.dispose();
    }
}
