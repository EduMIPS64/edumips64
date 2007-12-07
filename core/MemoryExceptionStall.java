package edumips64.core;
public class MemoryExceptionStall extends Exception{
    int stallNumber;
    
    public MemoryExceptionStall(int stalls){
        stallNumber=stalls;
    }
    
    public int getStalls() {
		return stallNumber;
	}
    
    }