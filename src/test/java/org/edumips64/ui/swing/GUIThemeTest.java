package org.edumips64.ui.swing;

import org.edumips64.BaseTest;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for {@link GUITheme}.
 *
 * <p>Historically, {@code usingDarkTheme} and the four cached colors were
 * {@code static} mutable fields on {@code GUITheme}. As of the
 * {@code AssignmentToNonFinalStatic} cleanup, {@code usingDarkTheme} and
 * {@code config} are per-instance: two coexisting {@code GUITheme} objects
 * with different config snapshots must report different colors. These tests
 * pin that contract so the field-scope refactor is not silently undone.
 */
public class GUIThemeTest extends BaseTest {

  private GUITheme themeWith(boolean dark) {
    ConfigStore store = new InMemoryConfigStore(ConfigStore.defaults);
    store.putBoolean(ConfigKey.UI_DARK_THEME, dark);
    return new GUITheme(store);
  }

  @Test
  public void lightAndDarkInstancesReportDifferentTextColors() {
    GUITheme light = themeWith(false);
    GUITheme dark = themeWith(true);
    assertNotEquals(
        "Light- and dark-theme instances must produce different text colors;"
            + " if these match, theme state has likely leaked back to a static field.",
        light.getTextColor(), dark.getTextColor());
  }

  @Test
  public void lightAndDarkInstancesReportDifferentBackgroundColors() {
    assertNotEquals(themeWith(false).getBackgroundColor(),
        themeWith(true).getBackgroundColor());
  }

  @Test
  public void instantiatingDarkThemeDoesNotMutateExistingLightInstance() {
    // Regression guard for the static→instance migration: creating a dark
    // theme after a light one must not change the light instance's colors.
    GUITheme light = themeWith(false);
    java.awt.Color textBefore = light.getTextColor();
    java.awt.Color bgBefore = light.getBackgroundColor();

    @SuppressWarnings("unused")
    GUITheme dark = themeWith(true);

    assertEquals(textBefore, light.getTextColor());
    assertEquals(bgBefore, light.getBackgroundColor());
  }

  @Test
  public void errorAndWarningColorsTrackThemeOfTheInstance() {
    GUITheme light = themeWith(false);
    GUITheme dark = themeWith(true);
    assertNotEquals(light.getErrorColor(), dark.getErrorColor());
    assertNotEquals(light.getWarningColor(), dark.getWarningColor());
  }
}
