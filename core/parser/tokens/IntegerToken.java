/* IntegerToken.java
 *
 * (c) 2008 Salvo Scellato
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
package edumips64.core.parser.tokens;
import edumips64.core.is.Instruction;

public class IntegerToken extends Token{
    public IntegerToken(String buffer, int line){
        super(buffer, line);
    }
    public IntegerToken(String buffer){
        super(buffer);
    }

    public boolean validate(char pattern){
        int value = Integer.parseInt(buffer);
        return (pattern == 'I') || (pattern == 'G')
            || (pattern == 'U' && value >= 0)
            || (pattern == 'C' && value >= 0 && value <= 7)
            || (pattern == 'L');
    }

    public void addToParametersList(Instruction instr) {
        System.out.println("Adding parameter " + Integer.parseInt(buffer));
        instr.addParam(Integer.parseInt(buffer));
    }
}

