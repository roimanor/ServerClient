/*
 * RegPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"
RegPacket::RegPacket(string regPack):Packet(11),msg(regPack) {
	// TODO Auto-generated constructor stub

}
string RegPacket::getString(){
	return msg;
}
RegPacket::~RegPacket() {
	// TODO Auto-generated destructor stub
}

void RegPacket::setPassword(string pass){
        msg.append(" ");
        msg.append(pass);
    
}
