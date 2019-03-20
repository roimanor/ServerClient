#include "../include/Packet.h"


WrqPacket::WrqPacket(string filename):Packet(2),filename(filename) {
	// TODO Auto-generated constructor stub
}

string WrqPacket::getString(){
	return filename;
}

void WrqPacket::setUsername(string user){
        filename.append(" ");
        filename.append(user);
}

string WrqPacket::getFilename(){
        return filename.substr(0,filename.find(" "));
}


WrqPacket::~WrqPacket() {
	// TODO Auto-generated destructor stub
}

