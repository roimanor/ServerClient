package bgu.spl171.net.api.bidi;

public class AckPacket extends Packet {

	private short block;
	
	public AckPacket(short block){
		this.block=block;
		super.opCode=4;
	}	
	
	public short getBlockNum(){
		return block;
	}
}
