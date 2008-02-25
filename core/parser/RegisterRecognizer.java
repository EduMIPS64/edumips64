/* RegisterRecognizer.java
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

public class RegisterRecognizer extends Recognizer{
    protected void buildTable(){
        numStates = 20;
        table = new ScannerTable<Character,Token>(numStates);

        table.setTransition(0,1, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'R' || c == 'r' || c == '$';
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

        //inizio riconscimento alias: lo stato 18 è lo stato di accettazione
        
        //riconoscitore {at, a0, a1, a2, a3}
        table.setTransition(1,7, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'a';
            }
        });
        table.setTransition(7,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 't' ||( c >= '0' && c <= '3');
            }
        });
        //riconoscitore {s0,..,s7, sp}
        table.setTransition(1,8, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 's';
            }
        });
        table.setTransition(8,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'p' ||( c >= '0' && c <= '7');
            }
        });
        //riconoscitore {t0,..,t9}
        table.setTransition(1,9, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 't';
            }
        });
        table.setTransition(9,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c >= '0' && c <= '9';
            }
        });
        //riconoscitore {v0,v1}
        table.setTransition(1,10, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'v';
            }
        });
        table.setTransition(10,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c >= '0' && c <= '1';
            }
        });
        //riconoscitore {k0,k1}
        table.setTransition(1,11, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'k';
            }
        });
        table.setTransition(11,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c >= '0' && c <= '1';
            }
        });
        //riconoscitore {gp}
        table.setTransition(1,12, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'g';
            }
        });
        table.setTransition(12,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'p';
            }
        });
        //riconoscitore {fp}
        table.setTransition(1,13, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'f';
            }
        });
        table.setTransition(13,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'p';
            }
        });
        //riconoscitore {ra}
        table.setTransition(1,14, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'r';
            }
        });
        table.setTransition(14,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'a';
            }
        });
        //riconoscitore {zero}
        table.setTransition(1,15, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'z';
            }
        });
        table.setTransition(15,16, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'e';
            }
        });
        table.setTransition(16,17, new Validator<Character>(){
            public boolean validate(Character c){
                return   c == 'r';
            }
        });
        table.setTransition(17,18, new Validator<Character>(){
            public boolean validate(Character c){
                return  c == 'o';
            }
        });

        Class c = new RegisterToken("").getClass();
        table.setFinalStatus(2,c);
        table.setFinalStatus(3,c);
        table.setFinalStatus(4,c);
        table.setFinalStatus(5,c);
        table.setFinalStatus(6,c);
        table.setFinalStatus(18,c);

    }
}
