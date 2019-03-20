/*
 * MessagingProtocol.h
 *
 *  Created on: Jan 18, 2017
 *      Author: roima
 */

#ifndef MESSAGINGPROTOCOL_H_
#define MESSAGINGPROTOCOL_H_
#include <iostream>
#include "Packet.h"
#include "EncoderDecoder.h"
#include "ConnectionHandler.h"
using namespace std;
class MessagingProtocol {
private:
	vector<DataPacket*>* data=new vector<DataPacket*>();
	vector<char>* dataReceived=new vector<char>();
	string filename;
	short lastBlockReceived;
	short lastBlock;
	
	bool tryingToDirq;
	bool tryingToLog;
	bool tryingToSend;
	bool tryingToDisconnect;
        bool tryingToRrq;
	bool logged;
	bool shouldterminate;
        //char* allData;
	//string writeName;
	EncoderDecoder EncDec;
	ConnectionHandler* connectionHandler;
public:
	MessagingProtocol(EncoderDecoder EncDec,ConnectionHandler *connectionHandler);
        MessagingProtocol(const MessagingProtocol &obj);
        MessagingProtocol& operator=(const MessagingProtocol &obj);
	virtual ~MessagingProtocol();
	bool shouldTerminate();
	void process(Packet* message);
};

#endif /* MESSAGINGPROTOCOL_H_ */
