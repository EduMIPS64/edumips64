// Stores the color values for light / dark themes.

package org.edumips64.ui.swing;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

import java.awt.*;

public class GUITheme {
    public static Color lightText = Color.black;
    public static Color lightBackground = Color.white;
    public static Color darkText = new Color(187, 187, 187);
    public static Color darkBackground = new Color(70, 73, 75);
    
    private static boolean usingDarkTheme;

    private ConfigStore config;

    public GUITheme(ConfigStore config){
        usingDarkTheme = config.getBoolean(ConfigKey.UI_DARK_THEME);
        this.config = config;
    }


    private static Color dimColor(Color color){
        return color.darker().darker();
    }

    public Color getColor(ConfigKey key){
        if(usingDarkTheme){
            return dimColor(new Color(config.getInt(key)));
        } else{
            return new Color(config.getInt(key));
        }
    }

    public Color getTextColor(){
        return usingDarkTheme ? darkText : lightText;
    }

    public Color getBackgroundColor(){
        return usingDarkTheme ? darkBackground : lightBackground;
    }
}
