/*
 * ErrorPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"

ErrorPacket::ErrorPacket(short ErrCode,string ErrMsg):Packet(5),ErrCode(ErrCode),ErrMsg(ErrMsg) {
	// TODO Auto-generated constructor stub

}
short ErrorPacket::getErrCode(){
	return ErrCode;
}
string ErrorPacket::getString(){
	return ErrMsg;
}

ErrorPacket::~ErrorPacket() {

}

