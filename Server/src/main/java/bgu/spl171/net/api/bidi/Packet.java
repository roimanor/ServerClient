package bgu.spl171.net.api.bidi;

public abstract class Packet {
	protected short opCode;
	
	public short getOp(){
		return opCode;
	}
	public String getString(){
		return "";
	}
}
