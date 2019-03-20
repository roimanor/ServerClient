/*
 * LogrqPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"

RenamePacket::RenamePacket(string files):Packet(12),files(files) {
	// TODO Auto-generated constructor stub

}
string RenamePacket::getString(){
	return files;
}

void RenamePacket::setUsername(string user){
        files.append(" ");
        files.append(user);
}

RenamePacket::~RenamePacket() {
	// TODO Auto-generated destructor stub
}

 
