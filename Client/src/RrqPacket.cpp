/*
 * RrqPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"

RrqPacket::RrqPacket(string filename):Packet(1),filename(filename) {
	// TODO Auto-generated constructor stub

}
string RrqPacket::getString(){
	return filename;
}

void RrqPacket::setUsername(string user){
        filename.append(" ");
        filename.append(user);
}
RrqPacket::~RrqPacket() {
	// TODO Auto-generated destructor stub
}

