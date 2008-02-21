/* FPRegisterRecognizer.java
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

public class FPRegisterRecognizer extends Recognizer{
    protected void buildTable(){
        numStates = 7;
        table = new ScannerTable<Character>(numStates);

        table.setTransition(0,1, new Validator<Character>(){
            public boolean validate(Character c){
                return c == 'F' || c == 'f';
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

        table.setFinalStatus(2, new FPRegisterToken("").getClass());
        table.setFinalStatus(3, new FPRegisterToken("").getClass());
        table.setFinalStatus(4, new FPRegisterToken("").getClass());
        table.setFinalStatus(5, new FPRegisterToken("").getClass());
        table.setFinalStatus(6, new FPRegisterToken("").getClass());
    }
}

