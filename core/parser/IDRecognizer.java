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
        numStates = 100;

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
                return !(c == 'R' || c == 'r' || c == 'F' || c == 'f' ||
                         c == 'P' || c == 'p' || c == 'N' || c == 'n' ||
                         c == 'Q' || c == 'q' || c == 'S' || c == 's') && 
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

        //parole chiave per i float number
        //prefisso per POSITIVEINFINITY e POSITIVEZERO
        String keyword = "POSITIVE";
        table.setTransition(0,14, new CharValidator(keyword.charAt(0)));

        //finisce nello stato 21
        for(int i = 1; i<keyword.length(); i++){
            table.setTransition(14+i-1,14+i, new CharValidator(keyword.charAt(i)));
            table.setTransition(14+i-1, 13, new NotCharValidator(keyword.charAt(i)));
            table.setFinalStatus(14+i-1, c_id);
        }

        //prefisso per NEGATIVEINFINITY e NEGATIVEZERO
        keyword = "NEGATIVE";
        table.setTransition(0,22, new CharValidator(keyword.charAt(0)));

        //finisce nello stato 28
        for(int i = 1; i<keyword.length()-1; i++){
            table.setTransition(22+i-1, 22+i, new CharValidator(keyword.charAt(i)));
            table.setTransition(22+i-1, 13, new NotCharValidator(keyword.charAt(i)));
            table.setFinalStatus(22+i-1, c_id);
        }

        //POSITIVE e NEGATIVE finiscono nello stesso stato 21
        table.setTransition(28, 21, new CharValidator(keyword.charAt(keyword.length()-1)));
        table.setTransition(28, 13, new NotCharValidator(keyword.charAt(keyword.length()-1)));
        table.setFinalStatus(21, c_id);

        //suffisso INFINITY (parte dalla transizione 21-->29 
        //e arriva fino allo stato 36)
        keyword = "INFINITY";
        table.setTransition(21, 29, new CharValidator(keyword.charAt(0)));



         

        for(int i = 1; i<keyword.length(); i++){
            table.setTransition(29+i-1, 29+i, new CharValidator(keyword.charAt(i)));
            table.setTransition(29+i-1, 13, new NotCharValidator(keyword.charAt(i)));
            table.setFinalStatus(29+i-1, c_id);
        }

        //suffisso ZERO (parte dalla transizione 21-->37 
        //e arriva fino allo stato 40)
        keyword = "ZERO";
        table.setTransition(21, 37, new CharValidator(keyword.charAt(0)));
        table.setTransition(21,13, new Validator<Character>(){
            public boolean validate(Character c){
                return  (!(c == 'Z' || c == 'z' || c == 'I' || c == 'i') &&
                        Character.isLetterOrDigit(c));
            }
        });

        for(int i = 1; i<keyword.length(); i++){
            table.setTransition(37+i-1, 37+i, new CharValidator(keyword.charAt(i)));
            table.setTransition(37+i-1, 13, new NotCharValidator(keyword.charAt(i)));
            table.setFinalStatus(37+i-1, c_id);
        }


        //prefisso Q e S per QNAN e SNAN
        table.setTransition(0,41, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'Q' || c == 'q' || c == 'S' || c == 's';
            }
        });

        keyword = "QNAN";
        //dallo stato 41 al 44
        for(int i = 1; i<keyword.length(); i++){
            table.setTransition(41+i-1, 41+i, new CharValidator(keyword.charAt(i)));
            table.setTransition(41+i-1, 13, new NotCharValidator(keyword.charAt(i)));
            table.setFinalStatus(41+i-1, c_id);
        }

        table.setFinalStatus(36, c_float); // fine INFINITY
        table.setFinalStatus(40, c_float); // fine ZERO
        table.setFinalStatus(44, c_float); // fine NAN

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

    public static void main(String[] args) throws java.io.IOException{
        IDRecognizer recognizer = new IDRecognizer();
        java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        while(true){
            System.out.print("Inserisci una stringa: ");
            String line = in.readLine();
            Token t = recognizer.recognize(new java.io.StringReader(line));
            System.out.println(t);
        }
    }
}




