/*
 * NullParsingAlgorithm.java
 *
 * Dummy parsing algorithm used to ignore sections of the code.
 *
 * (c) 2008 Andrea Spadaccini
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
package edumips64.core.parser;

class NullParsingAlgorithm extends ParsingAlgorithm {

    public NullParsingAlgorithm(Parser p) {
        super(p);
    }

    public void parse(Scanner s) {
        System.out.println("Starting NullParsingAlgorithm");
        while(s.hasToken()) {
            Token token = s.nextToken();
            String data = token.getBuffer();

            if(token.validate('D') && parser.hasAlgorithm(data))
                parser.switchParsingAlgorithm(data);
            else
                System.out.println("Throwing away: " + token.toString());
        }
    }
}
