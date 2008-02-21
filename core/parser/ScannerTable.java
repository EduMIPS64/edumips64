/* ScannerTable.java
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

import java.util.*;
import java.io.*;

/**
 * Represents a table to be used in a table-driven scanner that implements
 * a Deterministic Finite Automaton. Each transition between two states contains
 * a validating function to specifiy what element may trigger the transition.
 *
 * Given a state X and an element A, the next status of X under A SHOULD be
 * unique, otherways the behaviour is unpredictable. 
 * Scanned element are objects of class E, while upon recognition a 
 * Class< ? extends T> object is return to dynamically create an instance.
 */
public class ScannerTable<E,T>{
    
    /**
     * Represents a table entry as a transition with a 2-tuple:
     *  - a function to validate the membership of an element to a set
     *  - next status
     * By using a membership function it becomes easy to define a set instead
     * that using standard enumeration.
     */
    private class TableEntry<E> {
        private Validator<E> validator;
        private int nextStatus;
        
        public TableEntry(int nextStatus, Validator<E> validator){
            this.validator = validator;
            this.nextStatus = nextStatus;
        }

        public boolean validate(E element){
            return validator.validate(element);
        }

        public int nextStatus(){
            return nextStatus;
        }
    }
    
    //NOTE: it's impossible to use generic arrays, so we use an ArrayList
    private ArrayList<List<TableEntry<E>>> table;
    private int numStates;
    private boolean[] isFinalStatus;
    private ArrayList<Class<? extends T>> tokenClass;

    /** Standard constructor.
     * @param numStates number of states in the DFA
     */
    public ScannerTable(int numStates){
        this.numStates = numStates;
        isFinalStatus = new boolean[numStates];

        table = new ArrayList<List<TableEntry<E>>>(numStates);
        tokenClass = new ArrayList<Class<? extends T>>(numStates);
        for(int i = 0; i<numStates; i++){
            table.add(new ArrayList<TableEntry<E>>());
            tokenClass.add(null);
        }
    }

    /** Adds a transition to the table between two states: a Validator must be
     * passed to know when this transition is enabled.
     * @param status current status of the DFA
     * @param nextStatus next status of the DFA
     * @param validator object that tests membership for single elements
     */
    public void setTransition(int status, int nextStatus, Validator<E> validator){
        table.get(status).add(new TableEntry<E>(nextStatus, validator));
    }
    
    public void setFinalStatus(int status, Class<? extends T> clazz ){
        isFinalStatus[status] = true;
        tokenClass.set(status,clazz);
    }

    public boolean isFinalStatus(int status){
        if( status <0 || status > numStates)
            return false;
        else
            return isFinalStatus[status];
    }

    /** Returns the Token class associated with the final state status.
     * @param status current status of the DFA
     * @return a Class Object used to dynamically instantiate Token objects
     */
    public Class<? extends T> getFinalStatusClass(int status){
        return tokenClass.get(status);
    }


    /** Returns the next status of the DFA given the current status and the
     * element being analyzed.
     * @param status current status of the DFA
     * @param element element being analyzed by the scanner
     * @return next status in the DFA or -1 if an error has occurred
     */
    public int getNextStatus(int status, E element){
        for(TableEntry<E> entry : table.get(status)){
            if(entry.validate(element))
                return entry.nextStatus();
        }
        return -1;
    }
    
    public static void main(String[] args) throws IOException{
        final int NUMSTATES = 6;
        ScannerTable<Character,Token> table = new ScannerTable<Character,Token>(NUMSTATES);

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


        table.setFinalStatus(3, new IntegerToken("").getClass());
        table.setFinalStatus(4, new IntegerToken("").getClass());
        table.setFinalStatus(5, new IntegerToken("").getClass());

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.print("Inserisci una stringa: ");
            String line = in.readLine();
            int state = 0;
            char token;
                for(int index = 0; index < line.length(); index++){
                    token = line.charAt(index);
                    state = table.getNextStatus(state,token);
                    if(state == -1)
                        break;
                }
                if( table.isFinalStatus(state))
                    System.out.println("Intero riconosciuto: " + line);
                else
                    System.out.println("Intero non riconosciuto: " + line);
            }
        }
    }

