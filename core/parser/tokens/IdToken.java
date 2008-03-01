/* IdToken.java
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

import edumips64.core.is.*;
import edumips64.core.*;
import java.util.*;

public class IdToken extends Token{

    public static Map<String,Class> keywords =
        new TreeMap<String,Class>(String.CASE_INSENSITIVE_ORDER);

    static{
        Class c = new FloatToken("").getClass();
        keywords.put("POSITIVEINFINITY",c);
        keywords.put("NEGATIVEINFINITY",c);
        keywords.put("POSITIVEZERO",c);
        keywords.put("NEGATIVEZERO",c);
        keywords.put("QNAN",c);
        keywords.put("SNAN",c);
    }

    public IdToken(String buffer) {
        super(buffer);
    }

    public IdToken(String buffer, int line) {
        super(buffer,line);
    }
    public boolean validate(char pattern){
        return pattern == 'L' || pattern == 'B';
    }

    public void addToParametersList(Instruction instr) throws ParameterException{
        try{
            SymbolTable symTab = SymbolTable.getInstance();
            MemoryElement elem = symTab.getCell(buffer);
            instr.addParam(elem.getAddress());
        }
        catch(MemoryElementNotFoundException e){
            throw new ParameterException(this, "LABEL NOT FOUND");
        }
    }


}




