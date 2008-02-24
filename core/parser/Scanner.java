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
    private int currentLine;
    
    /** Constructor.
     * @param reader stream for consuming characters
     */
    public Scanner(Reader reader){
        this.reader = reader;
        this.currentLine = 1;
    }

    private Token getEOLToken(){
        return new EOLToken(currentLine++);
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
                return new EOFToken(currentLine);
            else
                return getEOLToken();
        }catch(IOException e){
            return new ErrorToken("I/O Error");
        }
    }

    // Debug method, prints the actual token
    public Token next() {
        Token t = nextToken();
        System.out.println(t);
        return t;
    }

    // TODO: eccezione END OF TOKEN STREAM
    public Token nextToken(){
        Token t;
        while(true){
            try{
                reader.mark(1);
                int r = reader.read();
                if( r == -1)
                    return new EOFToken(currentLine);

                char token = (char) r;
                
                //Pay attention, newline character is recognized as
                //whitespace...!
                if( token == '\n')
                    return getEOLToken();

                if( Character.isWhitespace(token))
                    continue;
                
                //registers and IDs start with a letter
                if( Character.isLetter(token)){
                    reader.reset();
                    //may be an identifier or a register
                    t = idRec.recognize(reader);
                    t.setLine(currentLine);
                    return t;
                }

                //numbers (integer and float) start with a digit or + and -
                if( Character.isDigit(token) || token == '+' || token == '-'){
                    reader.reset();
                    t = numRec.recognize(reader);
                    t.setLine(currentLine);
                    return t;
                }

                //single characters, comment, strings and directives
                //only start with certain characters
                switch(token){
                    case '(': return new LeftParenToken(currentLine);       
                    case ')': return new RightParenToken(currentLine);       
                    case ',': return new CommaToken(currentLine);
                    case ':': return new ColonToken(currentLine);
                    case ';': return skipComment(); 
                    case '$': reader.reset();
                              t = regRec.recognize(reader);
                              t.setLine(currentLine);
                              return t;
                    case '.': reader.reset(); 
                              t = directiveRec.recognize(reader);
                              t.setLine(currentLine);
                              return t;
                    case '"': reader.reset();
                              t = stringRec.recognize(reader);
                              t.setLine(currentLine);
                              return t;
                }
                return new ErrorToken(""+token, currentLine);
            } catch (IOException e){
                return new ErrorToken("I/O Error", currentLine);
            }
        }
    }

    public static void main(String[] args) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        Scanner scanner = new Scanner(reader);
        while(scanner.hasToken())
            System.out.println(scanner.nextToken());
    }
}

