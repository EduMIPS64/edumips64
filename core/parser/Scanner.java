/* Scanner.java
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

import java.io.*;

public class Scanner{
    private Reader reader;
    private DirectiveRecognizer directiveRec = new DirectiveRecognizer();
    private NumberRecognizer numRec = new NumberRecognizer();
    private StringRecognizer stringRec = new StringRecognizer();
    private RegisterRecognizer regRec = new RegisterRecognizer();
    //private FPRegisterRecognizer fpRegRec = new FPRegisterRecognizer();
    private IDRecognizer idRec = new IDRecognizer();
    
    /** Constructor.
     * @param reader stream for consuming characters
     */
    public Scanner(Reader reader){
        this.reader = reader;
    }

    /** Returns true only if the scanner has a new token.
     */
    public boolean hasToken(){
        try{
            reader.mark(1);
            int r = reader.read();
            if( r == -1)
                return false;
            else{
                reader.reset();
                return true;
            }
        } catch (IOException e){
            return false;
        }
    }

    private Token skipComment(){
        int r;
        try{
            do{
                r = reader.read();
            }while (r != -1 && (char)r != '\n');

            //A comment line may end with EOL or with EOF
            if( r == -1)
                return new EOFToken();
            else
                return new EOLToken();
        }catch(IOException e){
            return new ErrorToken("I/O Error");
        }
    }

    public Token nextToken(){
        while(true){
            try{
                reader.mark(1);
                int r = reader.read();
                if( r == -1)
                    return new EOFToken();

                char token = (char) r;
                
                if( Character.isWhitespace(token))
                    continue;
                
                //registers and IDs start with a letter
                if( Character.isLetter(token)){
                    reader.reset();
                    //may be an identifier or a register
                    return idRec.recognize(reader);
                }

                //numbers (integer and float) start with a digit or + and -
                if( Character.isDigit(token) || token == '+' || token == '-'){
                    reader.reset();
                    return numRec.recognize(reader);
                }

                //single characters, comment, strings and directives
                //only start with certain characters
                switch(token){
                    case '\n': return new EOLToken();
                    case '(': return new LeftParenToken();       
                    case ')': return new RightParenToken();       
                    case ',': return new CommaToken();
                    case ':': return new ColonToken();
                    case ';': return skipComment(); 
                    case '$': reader.reset();
                              return regRec.recognize(reader);
                    case '.': reader.reset(); 
                              return directiveRec.recognize(reader);
                    case '"': reader.reset();
                              return stringRec.recognize(reader);
                }
                return new ErrorToken(""+token);
            } catch (IOException e){
                return new ErrorToken("I/O Error");
            }
        }
    }

    public static void main(String[] args) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        Scanner scanner = new Scanner(reader);
        while(scanner.hasToken())
            System.out.println(scanner.nextToken());
        System.out.println(scanner.nextToken());
    }
}

