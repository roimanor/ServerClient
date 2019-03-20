/*
 * EncoderDecoder.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/EncoderDecoder.h"
#include "../include/Packet.h"
#include <vector>


EncoderDecoder::EncoderDecoder():len(0),opCode(0) {
}
EncoderDecoder::EncoderDecoder(const EncoderDecoder &obj):len(0),opCode(0){
}
EncoderDecoder& EncoderDecoder::operator=(const EncoderDecoder &obj){
    return *this;
}
Packet* EncoderDecoder::decodeNextByte(char nextByte){

	Packet* packet=nullptr;
	bytes->push_back(nextByte);
	if (bytes->size()==2){
		opCode=bytesToShort(bytes->data());
	}
	else if (bytes->size()==4 && opCode==4){
		char* blockBytes= new char[2];
		blockBytes[0]=bytes->at(2);
		blockBytes[1]=bytes->at(3);
		short block=bytesToShort(blockBytes);
		packet= new AckPacket(block);

		reset();
	}
	else if(opCode==3 && bytes->size() > 4){
		char* sizeBytes=new char[2];
		sizeBytes[0]=bytes->at(2);
		sizeBytes[1]=bytes->at(3);
		unsigned short size=bytesToShort(sizeBytes);
		if(size == (bytes->size())-6){
			char* blockNumBytes=new char[2];
			blockNumBytes[0]=bytes->at(4);
			blockNumBytes[1]=bytes->at(5);
			char* dataBytes=new char[size];
			for(unsigned int i=0;i<size; i++){
				dataBytes[i]=bytes->at(6+i);
			}
			packet= new DataPacket(dataBytes,size,bytesToShort(blockNumBytes));
			reset();
		}
	}
	else if (bytes->size()>3 && nextByte == '\0'){
		switch(opCode){
		case 5:{
			char* errorCodeBytes=new char[2];
			errorCodeBytes[0]=bytes->at(2);
			errorCodeBytes[1]=bytes->at(3);
			char* msg=new char[bytes->size()-5];
			for(unsigned int i=0;i<(bytes->size()-5);i++)
				msg[i]=bytes->at(4+i);
			packet= new ErrorPacket(bytesToShort(errorCodeBytes), string(msg));
			reset();
			break;
		}
		case 9:{
			char* msg=new char[bytes->size()-4];
			for(unsigned int i=0;i<(bytes->size()-4);i++)
				msg[i]=bytes->at(3+i);
                       // cout <<"msg "<<msg <<endl;
			packet= new BCastPacket((bytes->at(2)), string(msg));
			reset();
			break;

		}
		}
	}
	return packet;

}

	vector<char>* EncoderDecoder::encode(Packet* message){
           
		short opNum=message->getOpCode();
		vector<char>* allBytes=new vector<char>();
		char opToBytes[2];
		shortToBytes(opNum,opToBytes);
		allBytes->push_back((short)opToBytes[0]);
		allBytes->push_back((short)opToBytes[1]);
		if(opNum == 1){
			string temp=(static_cast<RrqPacket*>(message))->getString();
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		if(opNum == 2){
			string temp=(static_cast<WrqPacket*>(message))->getString();
			//allBytes=new char[3 + temp.length()];
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		if(opNum == 7){
			string temp=(static_cast<LogRqPacket*>(message))->getString();
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		if(opNum == 11){
			string temp=(static_cast<RegPacket*>(message))->getString();
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		
		if(opNum == 12){
			string temp=(static_cast<RenamePacket*>(message))->getString();
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		
		if(opNum == 8){
			string temp=(static_cast<DelRqPacket*>(message))->getString();
			for(unsigned int i=0;i<temp.length();i++)
				allBytes->push_back(temp.at(i));
			allBytes->push_back('\0');
		}
		else
		if(opNum == 3)
		{

			DataPacket* dataPack=static_cast<DataPacket*>(message);

			short size=dataPack->getPacketSize();
			char* dataSizeBytes=new char[2];
			shortToBytes(size,dataSizeBytes);
			char* blockNum=new char[2];
			shortToBytes(dataPack->getBlockNum(),blockNum);

			char* data=dataPack->getData();
			allBytes->push_back(dataSizeBytes[0]);
			allBytes->push_back(dataSizeBytes[1]);
			allBytes->push_back(blockNum[0]);
			allBytes->push_back(blockNum[1]);
			for(int i=0;i<dataPack->getPacketSize();i++)
				allBytes->push_back(data[i]);

		}
		else
		if(opNum == 4)
		{
			AckPacket* ackPack=static_cast<AckPacket*>(message);
			short blockNum=ackPack->getBlockNum();
			char* blockNumBytes=new char[2];
			shortToBytes(blockNum,blockNumBytes);
			allBytes->push_back(blockNumBytes[0]);
			allBytes->push_back(blockNumBytes[1]);
		}
		else
		if(opNum == 5)
		{
			ErrorPacket* errorPack=static_cast<ErrorPacket*>(message);
			string error=errorPack->getString();
			short errorNum=errorPack->getErrCode();

			char* errorNumBytes=new char[2];
			shortToBytes(errorNum,errorNumBytes);
			allBytes->push_back(errorNumBytes[0]);
			allBytes->push_back(errorNumBytes[1]);
			for(unsigned int i=0;i<error.length();i++)
				allBytes->push_back(error.at(i));
		}

		return allBytes;
	}

EncoderDecoder::~EncoderDecoder() {
	// TODO Auto-generated destructor stub
}

void EncoderDecoder::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] =((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);

}
short EncoderDecoder::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
void EncoderDecoder::reset(){
	bytes->clear();
	 len=0;
	 opCode=0;
}
