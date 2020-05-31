/*
 * ParsedInstructionMetadata.java
 * 
 * Contains instruction metadata which is generated at parsing time.
 *
 * (c) 2020 Andrea Spadaccini and the EduMIPS64 project.
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
package org.edumips64.core.is;

public class ParsedInstructionMetadata {
    public final int sourceLine;
    public final long address;

    public ParsedInstructionMetadata(int sourceLine, long address) {
        this.address = address;
        this.sourceLine = sourceLine;
    }
}