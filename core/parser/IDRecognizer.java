/* IDRecognizer.java
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

//queste due classi servono per implementare velocemente il riconoscimento di un
//carattere oppure il riconoscimento di tutti i caratteri ammessi tranne un
//carattere
class CharValidator implements Validator<Character>{
    private Character pattern;
    public CharValidator(char pattern){
        this.pattern = pattern;
    }
    public boolean validate(Character element){
        return Character.toUpperCase(element) == 
            Character.toUpperCase(pattern);
    }
}

class NotCharValidator implements Validator<Character>{
    private Character pattern;
    public NotCharValidator(char pattern){
        this.pattern = pattern;
    }
    public boolean validate(Character element){
        return (
                !(Character.toUpperCase(element) == 
                Character.toUpperCase(pattern))
                )
            && 
            (
             Character.isLetterOrDigit(element) || 
             element == '_' || element == '.'
             );
    }
}

public class IDRecognizer extends Recognizer{
    protected void buildTable(){
        numStates = 14;

        Class c_float = new FloatToken("").getClass();
        Class c_id = new IdToken("").getClass();
        Class c_reg = new RegisterToken("").getClass();
        Class c_fpReg = new FPRegisterToken("").getClass();

        table = new ScannerTable<Character,Token>(numStates);
        
        //riconoscimento registro
        table.setTransition(0,1, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'R' || c == 'r';
            }
        });
        table.setTransition(1,2, new Validator<Character>(){
            public boolean validate(Character c){
                return c >= '0' && c <= '2';
            }
        });
        table.setTransition(1,3, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '3';
            }
        });
        table.setTransition(1,4, new Validator<Character>(){
            public boolean validate(Character c){
                return   c >= '4' && c <= '9';
            }
        });
        table.setTransition(2,5, new Validator<Character>(){
            public boolean validate(Character c){
                return   c >= '0' && c <= '9';
            }
        });
        table.setTransition(3,6, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == '0' || c == '1';
            }
        });

        //riconoscimento registro FP
        table.setTransition(0,7, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'F' || c == 'f';
            }
        });
        table.setTransition(7,8, new Validator<Character>(){
            public boolean validate(Character c){
                return c >= '0' && c <= '2';
            }
        });
        table.setTransition(7,9, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '3';
            }
        });
        table.setTransition(7,10, new Validator<Character>(){
            public boolean validate(Character c){
                return   c >= '4' && c <= '9';
            }
        });
        table.setTransition(8,11, new Validator<Character>(){
            public boolean validate(Character c){
                return   c >= '0' && c <= '9';
            }
        });
        table.setTransition(9,12, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == '0' || c == '1';
            }
        });

        //inserimento transizioni dal riconoscitore di registri
        //al riconoscitore di ID
        table.setTransition(0,13, new Validator<Character>(){
            public boolean validate(Character c){
                return !(c == 'R' || c == 'r' || c == 'F' || c == 'f') &&
                        (Character.isLetter(c) || c == '_');
            }
        });
        table.setTransition(1,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   Character.isLetter(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(2,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   Character.isLetter(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(3,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   !(c == '0' || c == '1') && 
                          (Character.isLetterOrDigit(c) || c == '_' || c == '.');
            }
        });
        table.setTransition(4,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(5,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(6,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });

        //inserimento transizioni dal riconoscitore di registri FP
        //al riconoscitore di ID
        table.setTransition(7,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   Character.isLetter(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(8,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   Character.isLetter(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(9,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   !(c == '0' || c == '1') && 
                          (Character.isLetterOrDigit(c) || c == '_' || c == '.');
            }
        });
        table.setTransition(10,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(11,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });
        table.setTransition(12,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });

        //transizione del riconoscitore di ID
        table.setTransition(13,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  Character.isLetterOrDigit(c) || c == '_' || c == '.';
            }
        });

        table.setFinalStatus(2, c_reg);
        table.setFinalStatus(3, c_reg);
        table.setFinalStatus(4, c_reg);
        table.setFinalStatus(5, c_reg);
        table.setFinalStatus(6, c_reg);

        table.setFinalStatus(8, c_fpReg);
        table.setFinalStatus(9, c_fpReg);
        table.setFinalStatus(10, c_fpReg);
        table.setFinalStatus(11, c_fpReg);
        table.setFinalStatus(12, c_fpReg);

        table.setFinalStatus(1,c_id);
        table.setFinalStatus(7, c_id);
        table.setFinalStatus(13, c_id);
    }

    //questo metodo va ridefinito perchè dopo aver riconosciuto un ID si deve
    //verificare se è una parola chiave
    public Token recognize(java.io.Reader stream){
        Token t = super.recognize(stream); //riconoscimento della classe base
        String key = t.getBuffer();

        //verifica nel dizionario delle parole chiave
        if(IdToken.keywords.containsKey(key)){
            Class c = IdToken.keywords.get(key);
            return createToken(c);
        }
        else
            return t;
    }
}




