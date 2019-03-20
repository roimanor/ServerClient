/*
 * DataPacket.cpp
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#include "../include/Packet.h"

DataPacket::DataPacket(char* data,short PacketSize,short BlockNum):Packet(3),data(data),PacketSize(PacketSize),BlockNum(BlockNum) {
}
DataPacket::DataPacket(const DataPacket &obj):Packet(3),data(),PacketSize(obj.PacketSize),BlockNum(obj.BlockNum){
    
}
DataPacket& DataPacket::operator=(const DataPacket &obj){
    return *this;
    
}
short DataPacket::getPacketSize(){
	return PacketSize;
}
short DataPacket::getBlockNum(){
	return BlockNum;
}
char* DataPacket::getData(){
	return data;
}
DataPacket::~DataPacket() {

}

