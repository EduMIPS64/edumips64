package org.edumips64.utils;

import org.edumips64.BaseTest;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConfigStoreTest extends BaseTest {
    @Test()
    public void testEmptyMap() throws Exception {
        Map<ConfigKey, Object> empty = new HashMap<>();
        assertTrue(ConfigStore.isValid(empty));
    }

    @Test()
    public void testHasOneValue() throws Exception {
        Map<ConfigKey, Object> one = new HashMap<>();
        one.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
        assertTrue(ConfigStore.isValid(one));

        one = new HashMap<>();
        one.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, true);
        assertTrue(ConfigStore.isValid(one));
    }

    @Test()
    public void testValidCouples() throws Exception {
        Map<ConfigKey, Object> both = new HashMap<>();
        both.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
        both.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, false);
        assertTrue(ConfigStore.isValid(both));

        both = new HashMap<>();
        both.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
        both.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, true);
        assertTrue(ConfigStore.isValid(both));
    }

    @Test()
    public void testInvalidCombination() throws Exception {
        Map<ConfigKey, Object> both = new HashMap<>();
        both.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
        both.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, true);
        assertFalse(ConfigStore.isValid(both));
    }

    @Test(expected = ConfigStoreTypeException.class)
    public void testInvalidTypeMasked() throws Exception {
        Map<ConfigKey, Object> both = new HashMap<>();
        both.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, 10);
        both.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, true);
        ConfigStore.isValid(both);
    }

    @Test(expected = ConfigStoreTypeException.class)
    public void testInvalidTypeTerminate() throws Exception {
        Map<ConfigKey, Object> both = new HashMap<>();
        both.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, true);
        both.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, 10);
        ConfigStore.isValid(both);
    }
}