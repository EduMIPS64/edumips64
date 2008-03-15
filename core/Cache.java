/* Cache.java
 *
 * This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * (c) 2008 Samuele Di Cataldo
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

package edumips64.core;

import edumips64.utils.*;
import java.util.*;
import edumips64.core.is.*;
import java.io.*;
import java.lang.*;
public class Cache{
        private static Cache cache = null; 
        private Memory mem;
        //Mi permettere di conoscere il numero di stal in corrispondenza di un miss.
        int stalls=0;
        //Mi permette di tenere traccia se un dato e in cache o meno in relazione al suo indirizzo.
        private Map<Integer, Boolean> CacheControl;
        
        private Cache(){
            mem = Memory.getInstance();
            CacheControl = new HashMap<Integer, Boolean>();
            for(int i = 0; i < CPU.DATALIMIT; i++)
                CacheControl.put(i,false);
            }
            
        public static Cache getInstance(){
		if( cache == null)
			cache = new Cache();
		return cache;
	}
        
        public void resetMap(){
                for(int i = 0; i < CPU.DATALIMIT; i++)
                CacheControl.put(i,false);
            }
        
      public int readB(int address) throws MemoryElementNotFoundException,MemoryExceptionStall
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readB(address);
            }
     
     public int readBU(int address) throws MemoryElementNotFoundException,MemoryExceptionStall
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readBU(address);
            }
            
    public String readD(int address) throws MemoryElementNotFoundException,MemoryExceptionStall
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readD(address);
            }
        
            
    public int readH(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularStringOfBitsException,NotAlingException
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readH(address);
            }
            
            
     public int readHU(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readHU(address);
            }
            
    
    public int readW(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readW(address);
            }
            
            
    public long readWU(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
            {
            
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else return mem.readWU(address);
            }
            
            
    public void writeB(int address, int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else mem.writeW(address,data);
            }
    
        
    public void writeD(int address, String data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularStringOfBitsException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else mem.writeD(address,data);
            }
            
            
     public void writeH(int address,int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else mem.writeH(address,data);
            }
            
            
            
    public void writeW(int address,int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                CacheControl.put(index,true);
                //Al momento suppongo 2 stalli in caso di miss
                throw new MemoryExceptionStall(2);
                }
            else mem.writeW(address,data);
            }
            
            
            
    //Funzione che genera un numero random[tra 1 e 2]di stalli.AL MOMENTO NON UTILIZZATA potenzialemente da sostituire ai 2 stalli posti di default in presenza di un miss dati.
     public int chekCache(int address){
             int stalls=(int)Math.random()+1;
             return stalls;
     }

 }
     
     