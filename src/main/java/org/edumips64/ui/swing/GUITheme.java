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
        
        // For Current Setting Exporting
        // System.out.println(config.getInt(ConfigKey.IF_COLOR));
        // System.out.println(config.getInt(ConfigKey.ID_COLOR));
        // System.out.println(config.getInt(ConfigKey.EX_COLOR));
        // System.out.println(config.getInt(ConfigKey.MEM_COLOR));
        // System.out.println(config.getInt(ConfigKey.FP_ADDER_COLOR));
        // System.out.println(config.getInt(ConfigKey.FP_MULTIPLIER_COLOR));
        // System.out.println(config.getInt(ConfigKey.FP_DIVIDER_COLOR));
        // System.out.println(config.getInt(ConfigKey.WB_COLOR));
        // System.out.println(config.getInt(ConfigKey.RAW_COLOR));
        // System.out.println(config.getInt(ConfigKey.SAME_IF_COLOR));
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

    public Color getErrorColor(){
        return usingDarkTheme ? new Color(183, 28, 28) : new Color(255, 138, 128);
    }

    public Color getWarningColor(){
        return usingDarkTheme ? new Color(245, 127, 23) : new Color(255, 255, 141);
    }
}
