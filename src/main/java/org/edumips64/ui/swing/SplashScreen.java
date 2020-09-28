/* SplashScreen.java
 *
 * SplashScreen for EduMIPS64
 * (c) 2006 Andrea Spadaccini, Antonella Scandura, Vanni Rizzo
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
