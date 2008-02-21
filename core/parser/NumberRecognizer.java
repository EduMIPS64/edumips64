/* NumberRecognizer.java
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

public class NumberRecognizer extends Recognizer{
    protected void buildTable(){
        numStates = 14;
        table = new ScannerTable<Character>(numStates);

        table.setTransition(0,1, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '+';
            }
        });
        table.setTransition(0,2, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '-';
            }
        });
        table.setTransition(0,3, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0';
            }
        });
        table.setTransition(0,4, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(1,4, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(1,3, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0'; 
            }
        });
        table.setTransition(2,4, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(2,3, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0'; 
            }
        });
        table.setTransition(4,5, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });
        table.setTransition(5,5, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });
        //fine riconoscimento interi, inizio riconoscimento 
        //porzione dopo la virgola
        table.setTransition(3,6, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '.'; 
            }
        });
        table.setTransition(4,6, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '.'; 
            }
        });
        table.setTransition(5,6, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '.'; 
            }
        });
        table.setTransition(6,7, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });
        table.setTransition(7,7, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });
        //esponente in notazione scientifica
        table.setTransition(7,8, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'e' || c == 'E';
            }
        });
        //inizio riconoscimento intero che rappresenta esponente
        table.setTransition(8,9, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '+';
            }
        });
        table.setTransition(8,10, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '-';
            }
        });
        table.setTransition(8,11, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0';
            }
        });
        table.setTransition(8,12, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(9,12, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(9,11, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0'; 
            }
        });
        table.setTransition(10,12, new Validator<Character>(){
            public boolean validate(Character c){
                return c != '0' && Character.isDigit(c); 
            }
        });
        table.setTransition(10,11, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '0'; 
            }
        });
        table.setTransition(12,13, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });
        table.setTransition(13,13, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isDigit(c); 
            }
        });

        table.setFinalStatus(3, new IntegerToken("").getClass());
        table.setFinalStatus(4, new IntegerToken("").getClass());
        table.setFinalStatus(5, new IntegerToken("").getClass());
        table.setFinalStatus(7, new FloatToken("").getClass());
        table.setFinalStatus(11, new FloatToken("").getClass());
        table.setFinalStatus(12, new FloatToken("").getClass());
        table.setFinalStatus(13, new FloatToken("").getClass());
    }
}

