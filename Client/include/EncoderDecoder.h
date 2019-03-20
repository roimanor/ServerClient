/*
 * EncoderDecoder.h
 *
 *  Created on: Jan 17, 2017
 *      Author: roima
 */

#ifndef ENCODERDECODER_H_
#define ENCODERDECODER_H_
#include<iostream>
#include "Packet.h"
#include <vector>
using namespace std;
class EncoderDecoder {
private:
	vector<char>* bytes=new vector<char>();;
	int len;
	short opCode;
	short bytesToShort(char* bytesArr);
	void shortToBytes(short num, char* bytesArr);
	void pushByte(char nextByte);
	void reset();
public:
	EncoderDecoder();
        EncoderDecoder(const EncoderDecoder& obj);
        EncoderDecoder& operator=(const EncoderDecoder& obj);
	virtual ~EncoderDecoder();
	Packet* decodeNextByte(char nextByte);
	vector<char>* encode(Packet* message);


};

#endif /* ENCODERDECODER_H_ */
