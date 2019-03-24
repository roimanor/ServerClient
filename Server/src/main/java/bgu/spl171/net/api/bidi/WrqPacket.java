package bgu.spl171.net.api.bidi;

public class WrqPacket extends Packet {

	private String fileName;
	
	public WrqPacket(String fileName){
		this.fileName=fileName;
		super.opCode=2;
	}
	
	public String getString(){
		return fileName;
	}
}
