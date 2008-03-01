/* RegisterToken.java
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

public class RegisterToken extends Token{

    private enum AliasRegister 
    {zero,at,v0,v1,a0,a1,a2,a3,t0,t1,t2,t3,t4,t5,t6,t7,s0,s1,s2,s3,s4,s5,s6,s7,t8,t9,k0,k1,gp,sp,fp,ra};

    public RegisterToken(String buffer,int line){
        super(buffer,line);
    }

    public RegisterToken(String buffer){
        super(buffer);
    }
    public boolean validate(char pattern){
        return pattern == 'R';
    }

    public void addToParametersList(Instruction instr){
        char first = buffer.charAt(0);
        int value = -1;

        //se inizia con R è un registro standard
        if(first == 'R' || first == 'r')
            try{
                value = Integer.parseInt(buffer.substring(1));
            }
        catch(NumberFormatException e){
            value = 0;
        }        
        //se inizia con $ può essere un registro o un alias
        else if( first == '$'){
            char second = buffer.charAt(1);
        
            if(Character.isDigit(second))  {//registro
                try{
                    value = Integer.parseInt(buffer.substring(1));
                }
                catch(NumberFormatException e){
                    value = 0;
                }
            }
            else{ //alias
                String alias = buffer.substring(1);
                for(AliasRegister x : AliasRegister.values())
                    if(alias.equalsIgnoreCase(x.name()))
                        value = x.ordinal();
            }
        }
        System.out.println("Adding parameter " + value);
        instr.addParam(value);
    }
}        



