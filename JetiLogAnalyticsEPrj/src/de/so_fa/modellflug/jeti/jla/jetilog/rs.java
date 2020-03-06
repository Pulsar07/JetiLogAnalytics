package de.so_fa.modellflug.jeti.jla.jetilog;

import java.util.StringTokenizer;

public class rs {

  public static void main(String[] args) {
	// TODO Auto-generated method stub

	long val = 4294967188L;
	Long lo = new Long(val);
//	System.out.println(":" + new Long(4294967188L));
//	System.out.println(":" +Long.highestOneBit(4294967188L));
//	System.out.println(":" +Long.lowestOneBit(4294967188L));
//	System.out.println(":" +Long.lowestOneBit(4294967187L));
//	System.out.println(":" +Long.lowestOneBit(4294967186L));
//	System.out.println(":" +Long.lowestOneBit(36L));
//	System.out.println(":" +Long.lowestOneBit(37L));
//	
//	System.out.println("-1:" +Long.parseLong("0"));
//	System.out.println(" 0:" +Long.parseLong("0"));
//	System.out.println("+1:" +Long.parseLong("1"));
//	System.out.println("+2:" +Long.parseLong("2"));
//	System.out.println("+3:" +Long.parseLong("3"));
//	System.out.println("+4:" +Long.parseLong("4"));
	
	long l;
	l=-4; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=-3; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=-2; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=-1; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=0; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=1; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=2; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=3; System.out.println(" "+l+":" + Long.toBinaryString(l));
	l=4294967186L; System.out.println(" "+l+":" + Long.toBinaryString(l) + " = " + (-l+1));
	
	l=4; System.out.println(" "+l+":" + Long.toBinaryString(l));
	
	
  }
}
