package net.za.cair.dip.test;

public class IntegerPair {
	public int lhs;
	public int rhs;
	
	public IntegerPair(int a, int b){
		lhs = a;
		rhs = b;
	}
	
	@Override
	public boolean equals(Object obj){
		IntegerPair pair = null;
		try{
			pair = (IntegerPair)obj;
		}
		catch(ClassCastException cce){
			return false;
		}
		if ((pair.lhs == this.lhs) && (pair.rhs == this.rhs))
			return true;
		return false;
	}
}
