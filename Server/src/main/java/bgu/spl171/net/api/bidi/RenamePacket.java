package bgu.spl171.net.api.bidi;

public class RenamePacket extends Packet {
	
	private String source;
	private String dest;
	
	public RenamePacket(String filerName){
		int ind=filerName.indexOf(' ');
		this.source=filerName.substring(0,ind);
		this.dest=filerName.substring(ind+1);
		super.opCode=12;
	}	
	
	public String getString(){
		return source+" "+dest;
	}
	public String getSource(){
		return source;
	}
	public String getDest(){
		return dest;
	}
	
}
