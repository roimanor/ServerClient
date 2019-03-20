/*
 * DelrqPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"

DelRqPacket::DelRqPacket(string filename):Packet(8),filename(filename) {
	// TODO Auto-generated constructor stub

}
string DelRqPacket::getString(){
	return filename;
}
void DelRqPacket::setUsername(string user){
        filename.append(" ");
        filename.append(user);
}
DelRqPacket::~DelRqPacket() {

}

