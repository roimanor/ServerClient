package bgu.spl171.net.api.bidi;

public class RegPacket extends Packet {

	private String user;
	
	public RegPacket(String user){
		this.user=user;
		super.opCode=11;
	}

	public String getString(){
		return user;
	}
}
