package bgu.spl171.net.api.bidi;

public class DataPacket extends Packet{

	private short PacketSize;
	private short BlockNum;
	private byte[] data;
	
	public DataPacket(short Packetsize, short BlockNum, byte[]data){
		super.opCode=3;
		this.PacketSize=Packetsize;
		this.BlockNum=BlockNum;
		this.data=data;
	}

	public short getBlockNum(){
		return BlockNum;
	}
	
	public short getPacketSize(){
		return PacketSize;
	}
	
	public byte[] getData(){
		return data;
	}
}
