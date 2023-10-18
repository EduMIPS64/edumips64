// Stores the color values for light / dark themes.

package org.edumips64.ui.swing;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

import java.awt.*;

public class GUITheme {
    private static Color lightText = Color.black;
    private static Color lightBackground = Color.white;
    private static Color darkText = new Color(187, 187, 187);
    private static Color darkBackground = new Color(70, 73, 75);
    
    private static boolean usingDarkTheme;

    private ConfigStore config;

    public GUITheme(ConfigStore config){
        usingDarkTheme = config.getBoolean(ConfigKey.UI_DARK_THEME);
        this.config = config;
    }

    // Make the current color darker.
    private static Color dimColor(Color color){
        return color.darker().darker();
    }

    // Get color from a specific setting key
    public Color getColor(ConfigKey key){
        if(usingDarkTheme){
            return dimColor(new Color(config.getInt(key)));
        } else{
            return new Color(config.getInt(key));
        }
    }

    // Get the text color under current theme
    public Color getTextColor(){
        return usingDarkTheme ? darkText : lightText;
    }

    // Get the background color under current theme
    public Color getBackgroundColor(){
        return usingDarkTheme ? darkBackground : lightBackground;
    }
}
