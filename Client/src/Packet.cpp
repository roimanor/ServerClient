#include "../include/Packet.h"

Packet::Packet(short opCode):opCode(opCode) {
	// TODO Auto-generated constructor stub
  
}
short Packet::getOpCode(){
	return opCode;
}
string Packet::getString(){
	return "";
}
Packet::~Packet() {
	// TODO Auto-generated destructor stub
}




