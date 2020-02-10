/* MainTest.java
 *
 * Tests for the Main class of EduMIPS64.
 *
 * (c) 2020 Andrea Spadaccini
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
package org.edumips64;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;

@RunWith(JUnit4.class)
public class MainTest {
    private FrameFixture window;

    @Before
    public void setUp() {
        Main main = GuiActionRunner.execute(() -> new Main());
        main.init();
        window = new FrameFixture(main.mainFrame);
        window.show();
    }

    @Test
    public void shouldBePossibleToExit() {
        window.menuItemWithPath("File", "Exit").click();
    }

    @After
    public void tearDown() {
        window.cleanUp();
    }
}