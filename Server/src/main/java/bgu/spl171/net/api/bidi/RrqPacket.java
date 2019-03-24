package bgu.spl171.net.api.bidi;

public class RrqPacket extends Packet{
	
	private String fileName;
	
	public RrqPacket(String fileName){
		this.fileName=fileName;
		super.opCode=1;
	}

	public String getString(){
		return fileName;
	}
}
