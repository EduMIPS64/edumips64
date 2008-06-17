package edumips64.core;

import edumips64.utils.*;

public class CacheElement extends BitSet64{
	private int address;
	private String comment;
	private String label;
	private String code;
        
    public CacheElement(int address){
		super();
		this.address = address;
		comment = "";
		label = "";
		code = "";
	}
    
    public int getAddress(){
		return address;
	}
    
    public String getComment(){
		return comment;
	}
    
    public void setComment(String comment){
		this.comment = comment;
	}
    
    public String getLabel(){
		return label;
	}
    
    public void setLabel(String label){
		this.label = label;
	}
    
    public String getCode(){
		return code;
	}
    
    public void setCode(String code){
		this.code = code;
	}
    
    public long getValue(){
		try{
			return Converter.binToLong(this.getBinString(),false);
		}
		catch(IrregularStringOfBitsException e){
			System.err.println("Errore in un registro");
			this.reset(false); //azzeriamo il registro
			return 0;
		}
	}
    
    public String toString() {
		try{
			String s = "ADDRESS " + Converter.binToHex(Converter.positiveIntToBin(32,this.getAddress()));
			s += ", VALUE " + Converter.binToHex(this.getBinString());
			s += ", LABEL  " + this.getLabel();
			s += ", CODE " + this.getCode();
			s += ", COMMENT " + this.getComment();
			return s;
		}
		catch(IrregularStringOfBitsException e){
			e.printStackTrace();

		}
		return "ERRORE";
	}
    
    public static void main(String[] args) throws Exception{
		CacheElement[] cache = new CacheElement[64];
		java.util.Random rand = new java.util.Random();
		int index = 0;
		java.util.List<BitSet64> list = new java.util.ArrayList<BitSet64>();
		for(CacheElement ca : cache){
			ca = new CacheElement(index*8);
			int value = rand.nextInt(65536) - 32768;
			ca.writeHalf(value);
			System.out.println("\nValue: " + value);
			System.out.println("CacheElement " + index);
			System.out.println("Address " + ca.getAddress());
			System.out.println("String: " + ca.getBinString());
			//System.out.println("Unsigned value: " + r.getValueUnsigned());
			System.out.println("Signed value: " + ca.getValue());
			index++;
			list.add(ca);
		}
	}
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    