package bgu.spl171.net.api.bidi;

public class LogRqPacket extends Packet {

	private String user;
	
	public LogRqPacket(String user){	
		this.user=user;
		super.opCode=7;
	}

	public String getString(){
		return user;
	}
}
