
#include "../include/Packet.h"

BCastPacket::BCastPacket(char delAdd,string fileName):Packet(9),delAdd(delAdd),filename(fileName) {


}
char BCastPacket::getDelAdd(){
	return delAdd;
}
string BCastPacket::getString(){
	return filename;
}
BCastPacket::~BCastPacket() {

}

