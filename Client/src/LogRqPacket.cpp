/*
 * LogrqPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"


LogRqPacket::LogRqPacket(string logPack):Packet(7),msg(logPack) {
	// TODO Auto-generated constructor stub

}
string LogRqPacket::getString(){
	return msg;
}

string LogRqPacket::getUsername(){
    return msg.substr(0,msg.find(" "));
}
LogRqPacket::~LogRqPacket() {
	// TODO Auto-generated destructor stub
}
void LogRqPacket::setPassword(string pass){
        msg.append(" ");
        msg.append(pass);
}
    

