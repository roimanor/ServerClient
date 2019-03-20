
#include <iostream>
#include "../include/MessagingProtocol.h"
#include <fstream>
#include <vector>
#include <string>
#include "../include/picosha2.h"
#include "../include/aes.hpp"
#include "../include/HMAC_SHA1.h"



int BLOCK=128;
string key,key1,key2,key3;
uint8_t iv[]  = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50 };
uint8_t *key1B;
unsigned char key2B[64];
struct AES_ctx ctx; 
BYTE digest[16] ;
CHMAC_SHA1 HMAC_SHA1 ;
string username;

//constructors

MessagingProtocol::MessagingProtocol(EncoderDecoder EncDec,ConnectionHandler *connectionHandler):
                filename(""),
                
                lastBlockReceived(0),
                tryingToRrq(false),
																								lastBlock(0),
																							EncDec(),
                                                                                                                                                                                        
                connectionHandler(connectionHandler){

}

MessagingProtocol::MessagingProtocol(const MessagingProtocol &obj):
                filename(""),
                
                lastBlockReceived(0),
                tryingToRrq(false),
																								lastBlock(0),
																								tryingToDirq(false),
																					EncDec(),
                                                                                                                                                                                        
                connectionHandler(obj.connectionHandler){
}
MessagingProtocol& MessagingProtocol::operator=(const MessagingProtocol &obj){
    return *this;
}
MessagingProtocol::~MessagingProtocol() {

}


bool MessagingProtocol::shouldTerminate() {
		return shouldterminate;
}

//main function to process message
void MessagingProtocol::process(Packet* message) {

	short opCode=message->getOpCode();


	switch(opCode){
	case 1:{ //RRQ   - READ REQUEST 
            //cast request 
		RrqPacket* readPack=static_cast<RrqPacket*>(message);
                //extract filename
		filename=readPack->getString();
		char* vec=EncDec.encode(message)->data();
                //send data to server
		connectionHandler->sendBytes(vec,EncDec.encode(message)->size());
                tryingToRrq=true;
		break;
	}
	case 2:{ //WRQ  - WRITE REQUEST 
		streampos size;
                data->clear();
                //cast request to correct packet 
		WrqPacket* writePack=static_cast<WrqPacket*>(message);
                //extract filename and set user nickname 
		string str=writePack->getFilename();
                writePack->setUsername(username);
		std::ifstream file (str.c_str(),std::fstream::binary);
		if (file.is_open())
		{
			file.seekg(0,ios::end);
			size = file.tellg();
			char* memblock = new char [size];
			char* currBlock;
			file.seekg (0, ios::beg);
			file.read (memblock, size);
			file.close();
			for(unsigned int i=0;i<(size/(BLOCK-16)+1);i++){
				if(i<size/(BLOCK-16)){
					currBlock=new char[BLOCK];
					for(unsigned int j=0;j<(uint)(BLOCK-16);j++)
                                            currBlock[j]=memblock[(BLOCK-16)*i+j];
                                        
                                        //each block is 128 bytes - 112 for encrypt data and 16 for HMAC
                                        
                                        //encrypt 112 bytes of data
                                        AES_CBC_encrypt_buffer(&ctx, (uint8_t*)(currBlock), BLOCK-16); 
                                        //add hmac to last 16 bytes
                                        HMAC_SHA1.HMAC_SHA1((BYTE *)currBlock,BLOCK-16 , key2B, key2.size(), digest) ;
                                        //copy data to buffer and send it to serer
                                        memcpy(currBlock + 112 ,digest , 16);
					data->push_back(new DataPacket(currBlock,BLOCK,i+1));
				}

				else {
                                    //to the same operations as before only this time data is smaller than 112 bytes
                                    //we pad the remaiming bytes with null 
                                        int mod = (size%(BLOCK - 16))%16;
					currBlock=new char[size%(BLOCK - 16)+16+mod];
					for(unsigned int j=0;j<size%(BLOCK-16);j++)
						currBlock[j]=memblock[(BLOCK-16)*i+j];
                                        
                                        for(unsigned int j=0;j<mod;j++)
						currBlock[size%(BLOCK-16)+j]=NULL ;
                                        
                                        AES_CBC_encrypt_buffer(&ctx, (uint8_t*)(currBlock), size%(BLOCK-16)); ///
                                        
                                        HMAC_SHA1.HMAC_SHA1((BYTE *)currBlock,size%(BLOCK-16)+mod , key2B, key2.size(), digest) ;
                                        memcpy(currBlock+(size%(BLOCK-16))+mod ,digest , 16);
					data->push_back(new DataPacket(currBlock,size%(BLOCK - 16)+16+mod,i+1));     
					delete(memblock);
				}
			}
			char* vec=EncDec.encode(message)->data();
			tryingToSend=true;
			connectionHandler->sendBytes(vec,EncDec.encode(message)->size());


		}
		else cout << "Unable to open file";
		break;
	}
	case 3:{ //DATA recieved from server 
                cout <<"GOT PACKET " <<lastBlockReceived<< endl;
                //gather all necessary information 
		lastBlockReceived++;
		DataPacket* dataPack=static_cast<DataPacket*>(message);
		short size=dataPack->getPacketSize();
		short block=dataPack->getBlockNum();
		char* data=dataPack->getData();   
                
                //if we are trying to read from server we need to check mac and decrypt if mac successful 
                if(!tryingToDirq && tryingToRrq) {
                    BYTE mac[16] ;
                    HMAC_SHA1.HMAC_SHA1((BYTE *)(data),size-16 , key2B, key2.size(), mac);
                    //generate mac and compare with old mac - check equal 0 if successful
                    int check = memcmp ( mac, data + (size-16), 16 );
                 
                    if(check != 0){
                        cout<< "HMAC FAILED! SERVER IS HOSTILE! DISCONNECTING FROM SERVER..." <<endl;
                        tryingToRrq=false;
                        tryingToDisconnect=true;
                        shouldterminate=true;
                        break;
                    }
                    //decrypt if hmac successful
                    AES_CBC_decrypt_buffer(&ctx, (uint8_t*)(data), size-16);
                }

                    //everything is O.K - push data
			for(int i=0;i<size-16;i++){
                            if(tryingToRrq || tryingToDirq)
				dataReceived->push_back(data[i]);
			}
			if(size < BLOCK){
                            //if it's the last block
				if(tryingToDirq){
                                    //if user wanted to see all files - add newline between every file
					for(int i=0;i<size;i++){
						cout<<data[i]<<flush;
						if(data[i] == '\0')
							cout<<endl;
					}
					//clean for next iteration
					dataReceived->clear();
					tryingToDirq=false;
					lastBlockReceived=0;
				}
				else
                                if(tryingToRrq){
                                    //if we are done reading write all data to the file and clean for next iteration
					cout<<"RRQ "<<filename<<" complete"<<endl;
					if(filename.compare("") != 0){
                                                ofstream file(filename, ofstream::binary);
						file.write(dataReceived->data(),dataReceived->size());
						file.flush();
						file.close();
						filename="";
						dataReceived->clear();
						lastBlockReceived=0;
                                                block = 0;
					}
				}
			}
			else {
                            //this isn't the last packet from server - send ACK packet that everything is O.K
				AckPacket* pack=new AckPacket(block);
				connectionHandler->sendBytes(EncDec.encode(pack)->data(),4);
			}
		
		break;
	}
	case 4:{ //ACK SOCKET
		AckPacket* ackPack=static_cast<AckPacket*>(message);
		unsigned short block=ackPack->getBlockNum();
		cout<<"ACK"<<block<<endl;
            //     cout <<"last block=" <<lastBlock <<"    block="<<block <<endl;
		if(tryingToSend) // if user requested to send a file
		{
			if(lastBlock == block){
				if(block<data->size()){
					char* vec=EncDec.encode(data->at(block))->data();
					connectionHandler->sendBytes(vec,EncDec.encode(data->at(block))->size());
					lastBlock++;
				}
				else{
					lastBlock=0;
                                }
			}
		}
		else
		if(tryingToLog)
		{
                    
			logged=true;
			tryingToLog=false;
		}
		else
		if(tryingToDisconnect)
		{
                    shouldterminate=true;                   
		}
		break;
	}
	case 5:{ //ERROR SOCKET
		tryingToSend=false;
                tryingToDisconnect=false;
                tryingToDirq=false;
		ErrorPacket* errorPack=static_cast<ErrorPacket*>(message);
		cout <<"Error "<<errorPack->getString()<<endl;
		break;
	}
	case 6:{ //DIRQ KB
		tryingToDirq=true;
                connectionHandler->sendBytes(EncDec.encode(message)->data(),2);
		break;
	}
	case 7:{//LOGRQ KB 
                //cast message to logPacket
                LogRqPacket* logPack=static_cast<LogRqPacket*>(message);
                //obtain user password
                key=logPack->getString();
                int ind=key.find(" ") + 1;
                key=key.substr(ind);
                
                //generate 3 secrets - key1 and key2 used for encryption and mac , key3 is used as password
                key1=picosha2::hash256_hex_string(key+"1");
                key2=picosha2::hash256_hex_string(key+"2");
                key3=picosha2::hash256_hex_string(key+"3");
                
                //get username 
                username = logPack->getUsername();
                
                std::vector<uint8_t> myVector(key1.begin(), key1.end()); // CAST KEY1
                key1B = &myVector[0];
                memcpy(key2B, key2.data(), key2.length());               // CAST KEY2
                AES_init_ctx_iv(&ctx, key1B, iv);
                
                //update user password to key3
                
                logPack->setPassword(key3);
                //send to server
		char* vec=EncDec.encode(message)->data();  
		tryingToLog=true;
		connectionHandler->sendBytes(vec,EncDec.encode(message)->size());

		break;
	}
	case 8:{//DELRQ KB
		char* vec=EncDec.encode(message)->data();
		connectionHandler->sendBytes(vec,EncDec.encode(message)->size());
		break;
                
	}
	case 9:{//BCAST SOCKET 
		BCastPacket* bCastPack=static_cast<BCastPacket*>(message);
		if(bCastPack->getDelAdd() == '\0')
			cout<<"BCAST del "<<bCastPack->getString()<<endl;
		else if(bCastPack->getDelAdd() == '\1'){
			cout<<"BCAST add "<<bCastPack->getString()<<endl;
                        tryingToSend=false;
                }
                else
                    cout<<"BCAST rename "<<bCastPack->getString()<<endl;
                
                
                lastBlock = 0;
                lastBlockReceived = 0;
		
                break;
	}
	case 10:{//DISC KB //TODO HASH HERE 
		tryingToDisconnect=true;
		connectionHandler->sendBytes(EncDec.encode(message)->data(),2);
		break;
	}
	case 11:{//REGRQ KB
               //cast packet to register packet
                RegPacket* regPack=static_cast<RegPacket*>(message);
               //get password from request
                key=regPack->getString();
                int ind=key.find(" ") + 1;
                key=key.substr(ind);
                //hash password and send to server
                key3=picosha2::hash256_hex_string(key+"3");
                regPack->setPassword(key3);
                char* vec=EncDec.encode(message)->data();
		connectionHandler->sendBytes(vec,EncDec.encode(message)->size());
		break;
	}
	case 12:{//RENAME KB
                RenamePacket* renamePack=static_cast<RenamePacket*>(message);
                renamePack->setUsername(username);
		char* vec=EncDec.encode(message)->data();
		connectionHandler->sendBytes(vec,EncDec.encode(message)->size());

		break;
	}
    }
}

