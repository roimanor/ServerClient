#include <iostream>
#ifndef PACKET_H_
#define PACKET_H_
using namespace std;
class Packet{
private:
	short opCode;
     
public:
	Packet(short opCode);
	virtual ~Packet();
	string getString();
	short getOpCode();
};

class RegPacket :public Packet{
private:
	string msg;
public:
	RegPacket(string regPack);
	virtual ~RegPacket();
	string getString();
        void setPassword(string pass);
};

class LogRqPacket :public Packet{
private:
	string msg;
public:
	LogRqPacket(string logPack);
	virtual ~LogRqPacket();
	string getString();
        string getUsername();
        void setPassword(string pass);
};



class AckPacket:public Packet{
private:
	short block;
public:
	AckPacket(short block);
	virtual ~AckPacket();
	short getBlockNum();
};



class BCastPacket:public Packet{
private:
	char delAdd;
	string filename;

public:
	BCastPacket(char delAdd,string filename);
	virtual ~BCastPacket();
	string getString();
	char getDelAdd();
};



class DataPacket :public Packet{
private:
        char* data;
	short PacketSize;
	short BlockNum;
	
public:
	DataPacket(char *data,short PacketSize,short BlockNum);
        DataPacket(const DataPacket &obj);
        DataPacket& operator=(const DataPacket &obj);
	virtual ~DataPacket();
	short getPacketSize();
	short getBlockNum();
	char* getData();
};



class DelRqPacket :public Packet{
private:
	string filename;
public:
	DelRqPacket(string filename);
	virtual ~DelRqPacket();
	string getString();
        void setUsername(string user);
};



class DirqPacket:public Packet {
public:
	DirqPacket();
	virtual ~DirqPacket();
};



class DiscPacket :public Packet{
public:
	DiscPacket();
	virtual ~DiscPacket();
};



class ErrorPacket:public Packet {
private:
        short ErrCode;
	string ErrMsg;
public:
	ErrorPacket(short Errcode,string ErrMsg);
	virtual ~ErrorPacket();
	string getString();
	short getErrCode();
};




class RenamePacket :public Packet{
private:
	string files;
public:
	RenamePacket(string files);
	virtual ~RenamePacket();
	string getString();
        void setUsername(string user);
};

class RrqPacket :public Packet{
private:
	string filename;
public:
	RrqPacket(string filename);
	virtual ~RrqPacket();
	string getString();
        void setUsername(string user);
};



class WrqPacket :public Packet{
private:
	string filename;
public:
	WrqPacket(string msg);
	virtual ~WrqPacket();
	string getString();
        void setUsername(string user);
        string getFilename();
};



#endif /* PACKET_H_ */
