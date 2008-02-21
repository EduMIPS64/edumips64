/* DirectiveRecognizer.java
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

public class DirectiveRecognizer extends Recognizer{
    protected void buildTable(){
        numStates = 3;
        table = new ScannerTable<Character,Token>(numStates);

        table.setTransition(0,1, new Validator<Character>(){
            public boolean validate(Character c){
                return c == '.';
            }
        });
        table.setTransition(1,2, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isLetter(c);
            }
        });

        table.setTransition(2,2, new Validator<Character>(){
            public boolean validate(Character c){
                return Character.isLetterOrDigit(c) || c == '_';
            }
        });

        table.setFinalStatus(2, new DirectiveToken("").getClass());
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




