#include <stdlib.h>

#include "../include/ConnectionHandler.h"
#include "../include/Packet.h"
#include "../include/MessagingProtocol.h"
#include <boost/thread.hpp>
using namespace std;
    ConnectionHandler* connectionHandler;
    EncoderDecoder* encoderDecoder;
    MessagingProtocol* messagingProtocol;
    string user = "";
    void KB(){
    	while(!messagingProtocol->shouldTerminate()){
    		bool good=false;
    		const short bufsize = 1024;
    		char buf[bufsize];
    		std::cin.getline(buf, bufsize);
    		std::string line(buf);
    		Packet* pack;
    		if(line.compare("DISC") == 0){
    			pack=new DiscPacket();
    			good=true;
                        
    		}
    		else
    		if(line.compare("DIRQ") == 0){
    			pack=new DirqPacket();
    			good=true;
    		}
    		else
    		if(line.substr(0,5).compare("DELRQ") == 0){
    			string str=line.substr(6,line.length()-1);
    			pack=new DelRqPacket(str);
    			good=true;
    		}
    		else
    		if(line.substr(0,5).compare("LOGRQ") ==0){
    			string str=line.substr(6,line.length()-1);
                        user = str.substr(0,str.find(" "));
    			pack=new LogRqPacket(str);
    			good=true;
    		}
    		else
    		if(line.substr(0,5).compare("REGRQ") ==0){
    			string str=line.substr(6,line.length()-1);
    			pack=new RegPacket(str);
    			good=true;
    		}
    		else
    		if(line.substr(0,3).compare("RRQ") == 0){
    			pack=new RrqPacket(line.substr(4,line.length()-1));
    			good=true;
    		}
    		else
    		if(line.substr(0,3).compare("WRQ") == 0){
    			pack=new WrqPacket(line.substr(4,line.length()-1));
    			good=true;
    		}
    		else
    		if(line.substr(0,4).compare("RNRQ") == 0){
    			pack=new RenamePacket(line.substr(5,line.length()-1));
    			good=true;
    		}
    		line="";
    		if(good)
    			messagingProtocol->process(pack);
    		else
    			cout <<"Unrecognized command"<<endl;
    	}
    }
    void Socket(){

    	while(!messagingProtocol->shouldTerminate()){
    		char * byte = new char[1];
    		if(connectionHandler->getBytes(byte,1)){
    			Packet* ans=(encoderDecoder->decodeNextByte(byte[0]));
    			if(ans!= nullptr)
    				messagingProtocol->process(ans);
    		}

    	}
    	connectionHandler->close();
    }
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    connectionHandler=new ConnectionHandler(host, port);
    connectionHandler->connect();
    encoderDecoder=new EncoderDecoder();
    messagingProtocol=new MessagingProtocol(*encoderDecoder,connectionHandler);

    /*if (!connectionHandler->connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }*/

    boost::thread thKB(KB);
    boost::thread thSocket(Socket);
    thSocket.join();
    thKB.join();


    return 0;
}
