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
import edumips64.core.IrregularWriteOperationException;
import edumips64.core.is.Instruction;
import edumips64.utils.Converter;

import java.util.*;

public class IntegerToken extends Token{

    protected static class TypeInfo {
        public TypeInfo(int dim, boolean sign) {
            size = dim;
            hasSign = sign;
        }
        int size;
        boolean hasSign;
    }

    // Association between a type and its info
    static protected Map<Character, TypeInfo> types; 

    static {
        types = new HashMap<Character, TypeInfo>();

        // Types for instructions
        types.put('C', new TypeInfo(3, false));
        types.put('I', new TypeInfo(16, true));
        types.put('U', new TypeInfo(16, false));

        // Types for memory
        types.put('L', new TypeInfo(64, false));
        types.put('N', new TypeInfo(64, true));
        types.put('G', new TypeInfo(64, true));
    }


    public IntegerToken(String buffer, int line, int column){
        super(buffer, line, column);
    }

    public IntegerToken(String buffer, int line){
        super(buffer, line);
    }

    public IntegerToken(String buffer){
        super(buffer);
    }

    public boolean validate(char pattern){
        if(!types.containsKey(pattern))
            return false;

        try{
            TypeInfo t = types.get(pattern);
            long value = Converter.parseInteger(buffer, t.size, t.hasSign);

            edumips64.Main.logger.debug("Using size " + t.size + " and hasSign " + t.hasSign);
            edumips64.Main.logger.debug("Validating " + value + " against " + pattern);

            if(t.size < 64) {
                long min, max;

                if(!t.hasSign) {
                    min = 0;
                    max = Converter.powLong(2, t.size);
                } else {
                    min = - Converter.powLong(2, t.size - 1);
                    max = Converter.powLong(2, t.size - 1) - 1;
                }

                return (value >= min) && (value <= max);
            }

            return true;
        }
        catch(IrregularWriteOperationException e){
            return false;
        }
    }

    public void addToParametersList(Instruction instr) {
        // We can explicitly cast to integer because we know that an
        // instruction will never hold a long in its parameters

        try{
            TypeInfo t = types.get(instr.getNextParameterType());
            instr.addParam((int)Converter.parseInteger(buffer, t.size, t.hasSign));
        }
        catch(IrregularWriteOperationException e){
            //we can NEVER enter here!
            throw new NumberFormatException();
        }
    }
}
