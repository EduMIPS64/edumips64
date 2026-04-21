/**
 * Enum-like object of all known setting keys used by the Web UI.
 *
 * This mirrors the `ConfigKey` enum in the Java simulator
 * (`org.edumips64.utils.ConfigKey`) and provides compile-time-ish checking
 * of keys: consumers should always import one of these constants rather than
 * using a raw string, so that typos surface at review time and renames can be
 * done safely.
 *
 * The *string value* of each key is the name used as the localStorage key
 * suffix (`edumips64:v1:<value>`). **Do not change these values**, because
 * that would orphan any value already persisted by previous versions of the
 * simulator for existing users.
 */
export const SettingKey = Object.freeze({
  // Editor / general UI settings.
  VI_MODE: 'viMode',
  FONT_SIZE: 'fontSize',
  ACCORDION_ALERTS: 'accordionAlerts',
  EXPANDED_ACCORDIONS: 'expandedAccordions',

  // Cache configuration.
  CACHE_L1D: 'cache.l1d',
  CACHE_L1I: 'cache.l1i',

  // Help dialog.
  HELP_LANGUAGE: 'help.language',
});
