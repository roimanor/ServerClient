package bgu.spl171.net.api.bidi;

public class BCastPacket extends Packet{
	
	private String fileName;
	private byte delAdd;
	
	public BCastPacket(byte delAdd, String fileName){
		this.fileName=fileName;
		super.opCode=9;
		this.delAdd=delAdd;
	}
	
	public byte getDelAdd(){
		return delAdd;
	}
	public String getFilename(){
		return fileName;
	}
	
}
