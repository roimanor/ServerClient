package bgu.spl171.net.api.bidi;

public class DelRqPacket extends Packet {
	
	private String fileName;
	
	public DelRqPacket(String filerName){
		this.fileName=filerName;
		super.opCode=8;
	}	
	
	public String getString(){
		return fileName;
	}
	
}
