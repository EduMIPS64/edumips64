/* LocalizationProvider.java
 *
 * Abstraction over the mechanism that loads localized UI messages.
 * (c) 2026 EduMIPS64
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
package org.edumips64.utils;

/**
 * Returns localized UI messages for a given language.
 *
 * <p>All concrete implementations are expected to read their data from the same
 * {@code CurrentLocaleMessages*.properties} files under {@code org/edumips64/utils/}, so that
 * those files are the single source of truth for the application's translations. Two
 * implementations exist:
 *
 * <ul>
 *   <li>{@link ResourceBundleLocalizationProvider} &mdash; used by the standard JVM build (Swing
 *       and CLI); loads messages through {@link java.util.ResourceBundle}.</li>
 *   <li>A GWT-only provider used by the web worker, which parses the same property files with a
 *       minimal in-house parser (GWT's JRE emulation doesn't include {@code ResourceBundle}). It
 *       lives under the GWT super-source tree.</li>
 * </ul>
 */
public interface LocalizationProvider {
  /**
   * Returns the localized message for {@code key} in language {@code lang}.
   *
   * <p>Fallback chain: selected language &rarr; English &rarr; the key itself (so the UI never
   * renders a blank string and missing translations are immediately obvious).
   *
   * @param lang internal language code as stored in {@link ConfigKey#LANGUAGE}
   *     (currently {@code "en"}, {@code "it"} or {@code "zhcn"})
   * @param key  the message key (e.g. {@code "Menu.FILE"})
   */
  String getString(String lang, String key);
}
