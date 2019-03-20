
#include "../include/Packet.h"

AckPacket::AckPacket(short block):Packet(4),block(block) {
	// TODO Auto-generated constructor stub

}
short AckPacket::getBlockNum(){
	return block;
}
AckPacket::~AckPacket() {
}

