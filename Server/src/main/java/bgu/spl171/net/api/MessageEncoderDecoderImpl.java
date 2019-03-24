package bgu.spl171.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl171.net.api.bidi.AckPacket;
import bgu.spl171.net.api.bidi.BCastPacket;
import bgu.spl171.net.api.bidi.DataPacket;
import bgu.spl171.net.api.bidi.DelRqPacket;
import bgu.spl171.net.api.bidi.DirqPacket;
import bgu.spl171.net.api.bidi.DiscPacket;
import bgu.spl171.net.api.bidi.ErrorPacket;
import bgu.spl171.net.api.bidi.LogRqPacket;
import bgu.spl171.net.api.bidi.Packet;
import bgu.spl171.net.api.bidi.RegPacket;
import bgu.spl171.net.api.bidi.RenamePacket;
import bgu.spl171.net.api.bidi.RrqPacket;
import bgu.spl171.net.api.bidi.WrqPacket;


public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Packet> {
	private byte[] bytes = new byte[1 << 10]; //start with 1k
	private int len = 0;
	private short opCode=0;

	@Override
	public Packet decodeNextByte(byte nextByte) {
		Packet packet=null;
		pushByte(nextByte);
		if (len==2){
			opCode=bytesToShort(bytes);
			if (opCode==6) {
				packet= new DirqPacket();
				reset();
			}
			else if (opCode==10){
				packet= new DiscPacket();
				reset();
			}
		}
		else if (len==4 && opCode==4){
			packet= new AckPacket(bytesToShort(Arrays.copyOfRange(bytes, 2, 4)));
			reset();
		}
		else if(opCode==3 && bytesToShort(Arrays.copyOfRange(bytes, 2, 4))==(len-6)){
			packet= new DataPacket(bytesToShort(Arrays.copyOfRange(bytes, 2, 4)),
					bytesToShort(Arrays.copyOfRange(bytes, 4, 6)), Arrays.copyOfRange(bytes, 6, len));
			reset();
		}
		else if (len>=2 && nextByte == 0){
			switch(opCode){
				case 1:{
					packet= new RrqPacket(popString(1));
					reset();
					break;
				}
				case 2:{
					packet= new WrqPacket(popString(2));
					reset();
					break;
				}
				case 5:{
					packet= new ErrorPacket(bytesToShort(Arrays.copyOfRange(bytes, 2, 4)), popString(5));
					reset();
					break;
				}
				case 7:{
					packet= new LogRqPacket(popString(7));
					reset();
					break;
				}
				case 8:{
					packet= new DelRqPacket(popString(8));
					reset();
					break;
				}
				case 9:{
					packet= new BCastPacket(bytes[2], popString(9));
					reset();
					break;
				}
				case 11:{
					packet= new RegPacket(popString(11));
	//				System.out.println(packet.getString());
					
					reset();
					break;
				}
				case 12:{
					packet= new RenamePacket(popString(8));
					reset();
					break;
				}
			}
		}
			return packet;
	}

	@Override
	public byte[] encode(Packet message) {
		short opNum=message.getOp();
		byte[] allBytes=null;
		byte[] opByte=shortToBytes(opNum);
		switch(opNum){
		case 1:
		case 2:
		case 7:
		case 8: {
			byte[] bytesName=message.getString().getBytes();
			allBytes= new byte[bytesName.length+3];
			System.arraycopy(opByte, 0, allBytes, 0, 2);
			System.arraycopy(bytesName, 0, allBytes, 2, bytesName.length);
			allBytes[allBytes.length-1]=0;
			break;
		}
		case 3:{
			short dataSize=((DataPacket)message).getPacketSize();
			byte[] PacketSize=shortToBytes(dataSize);
			byte[] blockNum=shortToBytes(((DataPacket)message).getBlockNum());
			allBytes=new byte[dataSize+6];
			System.arraycopy(opByte, 0, allBytes, 0, 2);
			System.arraycopy(PacketSize, 0, allBytes, 2, 2);
			System.arraycopy(blockNum, 0, allBytes, 4, 2);
			if (dataSize>0)
				System.arraycopy(((DataPacket)message).getData(), 0, allBytes, 6, dataSize);
			break;
		}
		case 4:{
			byte[] blockNum=shortToBytes(((AckPacket)message).getBlockNum());
			allBytes= new byte[4];
			System.arraycopy(opByte, 0, allBytes, 0, 2);
			System.arraycopy(blockNum, 0, allBytes, 2, 2);
			break;
		}
		case 5:{
			byte[] ErrMsg=((ErrorPacket)message).getErrMsg().getBytes();;
			byte[] ErrCode=shortToBytes(((ErrorPacket)message).getErrCode());
			allBytes=new byte[(ErrMsg.length)+5];
			System.arraycopy(opByte, 0, allBytes, 0, 2);
			System.arraycopy(ErrCode, 0, allBytes, 2, 2);
			System.arraycopy(ErrMsg, 0, allBytes, 4, ErrMsg.length);
			allBytes[allBytes.length-1]=0;
			break;
		}
		case 6:
		case 10:{
			allBytes=opByte;
			break;
		}
		case 9:{
			byte[] filename=((BCastPacket)message).getFilename().getBytes();
			allBytes=new byte[(filename.length)+4];
			System.arraycopy(opByte, 0, allBytes, 0, 2);
			allBytes[2]=((BCastPacket)message).getDelAdd();
			System.arraycopy(filename, 0, allBytes, 3, filename.length);
			allBytes[allBytes.length-1]=0;
			break;
		}
		}
		return allBytes;
	}
	 
	private void pushByte(byte nextByte) {
		if (len >= bytes.length) {
			bytes = Arrays.copyOf(bytes, len * 2);
		}

		bytes[len++] = nextByte;
	}
	
	 private String popString(int op) {
		 String result;
		 if (op==9) result = new String(bytes, 3, len-4, StandardCharsets.UTF_8);
		 else if (op==5) result = new String(bytes, 4, len-2, StandardCharsets.UTF_8);
		 else result = new String(bytes, 2, len-3, StandardCharsets.UTF_8);
	     return result;
	 }
	 
	 public short bytesToShort(byte[] byteArr)
	 {
	     short result = (short)((byteArr[0] & 0xff) << 8);
	     result += (short)(byteArr[1] & 0xff);
	     return result;
	 }
	 
	 public byte[] shortToBytes(short num)
	 {
	     byte[] bytesArr = new byte[2];
	     bytesArr[0] = (byte)((num >> 8) & 0xFF);
	     bytesArr[1] = (byte)(num & 0xFF);
	     return bytesArr;
	 }
	 
	 private void reset(){
		 len=0;
		 opCode=0;
	 }
	 
}