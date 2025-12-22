package org.edumips64.utils;

import org.edumips64.BaseTest;
import org.junit.Test;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

/**
 * Tests that the Messages resource bundle is correctly loaded for all 3 supported locales
 * (English, Italian, Chinese). This ensures translations for CLI options like --verbose
 * (added in PR #1483) are properly available in all languages.
 */
public class MessagesLocaleTest extends BaseTest {

    private static final String VERBOSE_KEY = "verbose";

    @Test
    public void testEnglishLocaleLoadsMessagesBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH);
        assertNotNull(bundle);
        String verboseMsg = bundle.getString(VERBOSE_KEY);
        assertNotNull(verboseMsg);
        assertTrue("English verbose message should contain 'Verbose mode'",
                verboseMsg.contains("Verbose mode"));
    }

    @Test
    public void testItalianLocaleLoadsMessagesBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.ITALIAN);
        assertNotNull(bundle);
        String verboseMsg = bundle.getString(VERBOSE_KEY);
        assertNotNull(verboseMsg);
        assertTrue("Italian verbose message should contain 'Modalità verbose'",
                verboseMsg.contains("Modalità verbose"));
    }

    @Test
    public void testChineseLocaleLoadsMessagesBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.SIMPLIFIED_CHINESE);
        assertNotNull(bundle);
        String verboseMsg = bundle.getString(VERBOSE_KEY);
        assertNotNull(verboseMsg);
        assertTrue("Chinese verbose message should contain '详细模式'",
                verboseMsg.contains("详细模式"));
    }

    @Test
    public void testAllKeysExistInAllLocales() {
        ResourceBundle enBundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH);
        ResourceBundle itBundle = ResourceBundle.getBundle("Messages", Locale.ITALIAN);
        ResourceBundle zhBundle = ResourceBundle.getBundle("Messages", Locale.SIMPLIFIED_CHINESE);

        // Verify all keys in English bundle exist in other locales
        for (String key : enBundle.keySet()) {
            assertNotNull("Italian bundle should have key: " + key,
                    getStringOrNull(itBundle, key));
            assertNotNull("Chinese bundle should have key: " + key,
                    getStringOrNull(zhBundle, key));
        }
    }

    private String getStringOrNull(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
