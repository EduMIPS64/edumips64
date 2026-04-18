package org.edumips64.utils;

import org.edumips64.BaseTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CurrentLocaleTest extends BaseTest {
    @Test()
    public void testDefaultsToEnglishWithNullConfig() throws Exception {
        CurrentLocale.setConfig(null);
        var val = CurrentLocale.getString("INVALIDVALUE");
        assertNotNull(val);
        assertEquals(val, "Invalid value");
    }

    @Test()
    public void testDefaultsToEnglishWithInvalidLanguage() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "invalid");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("INVALIDVALUE");
        assertNotNull(val);
        assertEquals(val, "Invalid value");
    }
     
    @Test()
    public void testReturnsEnglishWhenOtherLanguagesDontHaveTheMessage() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "it");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("TEST_MESSAGE_ONLY_IN_ENGLISH_DO_NOT_TRANSLATE");
        assertNotNull(val);
        assertEquals(val, "This message is only in English, used in unit tests, and should not be translated.");
    }

    @Test()
    public void testReturnsKeyWhenNotFound() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "en");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("NONEXISTINGKEY");
        assertNotNull(val);
        assertEquals(val, "NONEXISTINGKEY");
    }

    @Test()
    public void testEnglishLanguage() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "en");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("INVALIDVALUE");
        assertNotNull(val);
        assertEquals(val, "Invalid value");
    }

    @Test()
    public void testItalianLanguage() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "it");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("INVALIDVALUE");
        assertTrue(val != null);
        assertTrue(val.compareTo("Valore non valido") == 0);
    }

    @Test()
    public void testChineseLanguage() throws Exception {
        config.putString(ConfigKey.LANGUAGE, "zhcn");
        CurrentLocale.setConfig(config);
        var val = CurrentLocale.getString("INVALIDVALUE");
        assertTrue(val != null);
        assertTrue(val.compareTo("无效值") == 0);
    }
}
