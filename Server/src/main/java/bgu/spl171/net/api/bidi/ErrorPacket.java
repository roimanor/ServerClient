package bgu.spl171.net.api.bidi;

public class ErrorPacket extends Packet {
	
	private String ErrMsg;
	private short ErrCode;
	
	public ErrorPacket(short ErrCode, String ErrMsg){
		super.opCode=5;
		this.ErrMsg=ErrMsg;
		this.ErrCode=ErrCode;
	}	

	public short getErrCode(){
		return ErrCode;
	}
	
	public String getErrMsg(){
		return ErrMsg;
	}
}
