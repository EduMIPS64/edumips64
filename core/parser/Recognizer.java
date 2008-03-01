/* Recognizer.java
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
package edumips64.core.parser;

import edumips64.core.parser.tokens.*;
import java.io.*;

public abstract class Recognizer{
    protected ScannerTable<Character,Token> table;
    protected int numStates;
    protected StringBuffer buffer;

    public Recognizer(){
        buildTable();
    }

    protected abstract void buildTable();

    public Token recognize(Reader stream){
        buffer = new StringBuffer();
        int state = 0;
        int nextState = 0;
        char token;
            try{
                while(true){
                    stream.mark(2);
                    int r = stream.read();
                    if( r == -1) //end of stream
                        break;
                    token = (char)r;
                    nextState = table.getNextStatus(state,token);
                    if( nextState == -1){
                        stream.reset();
                        break;
                    }
                    state = nextState;
                    buffer.append(token);
                }
            }
            catch(IOException e){
                return new ErrorToken("I/O Error");
            }

            if( table.isFinalStatus(state)){
                Class<? extends Token> c = table.getFinalStatusClass(state);
                return createToken(c);
            }
            
            else
                return new ErrorToken(buffer.toString());
    }

    protected Token createToken(Class<? extends Token> c){
        try{
            java.lang.reflect.Constructor<? extends Token> ctor = c.getConstructor(
                    new Class[] {String.class});

            Token t = ctor.newInstance(
                    new Object[]{buffer.toString()});

            return t;
        }
        catch( Exception e){
            return new ErrorToken("Program error");
        }
    }

}










