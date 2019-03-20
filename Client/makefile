# define some Makefile variables for the compiler and compiler flags
# to use Makefile variables later in the Makefile: $()
CC = g++
CFLAGS  = -g -Wall -Weffc++ -std=c++11
LFLAGS  = -L/usr/lib -lboost_system -lboost_locale -lpthread -lboost_thread   

# All Targets
all: Client


# Tool invocations
Client: bin/Packet.o bin/BCastPacket.o bin/RenamePacket.o bin/DataPacket.o bin/RegPacket.o bin/AckPacket.o bin/DelRqPacket.o bin/DirqPacket.o bin/DiscPacket.o bin/ErrorPacket.o bin/LogRqPacket.o bin/RrqPacket.o bin/WrqPacket.o bin/ConnectionHandler.o bin/EncoderDecoder.o bin/MessagingProtocol.o bin/Client.o bin/aes.o bin/SHA1.o  bin/HMAC_SHA1.o
	@echo 'Building target: Client'
	@echo 'Invoking: C++ Linker'
	$(CC) -o bin/Client bin/Packet.o bin/BCastPacket.o bin/RenamePacket.o bin/RegPacket.o bin/AckPacket.o bin/DataPacket.o bin/DelRqPacket.o bin/DirqPacket.o bin/DiscPacket.o bin/ErrorPacket.o bin/LogRqPacket.o bin/RrqPacket.o bin/WrqPacket.o bin/ConnectionHandler.o bin/EncoderDecoder.o bin/MessagingProtocol.o bin/Client.o bin/aes.o bin/SHA1.o bin/HMAC_SHA1.o $(LFLAGS)
	@echo 'Finished building target: Client'
	@echo ' '
	

bin/HMAC_SHA1.o: src/HMAC_SHA1.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/HMAC_SHA1.o src/HMAC_SHA1.cpp

bin/SHA1.o: src/SHA1.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/SHA1.o src/SHA1.cpp	

bin/aes.o : src/aes.c include/aes.h
	gcc -Wall -Os -c -o $@ $<
	
bin/Packet.o: src/Packet.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Packet.o src/Packet.cpp
	
bin/BCastPacket.o: src/BCastPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/BCastPacket.o src/BCastPacket.cpp
	
bin/RenamePacket.o: src/RenamePacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/RenamePacket.o src/RenamePacket.cpp

bin/DataPacket.o: src/DataPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/DataPacket.o src/DataPacket.cpp
	
bin/DelRqPacket.o: src/DelRqPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/DelRqPacket.o src/DelRqPacket.cpp
	
bin/DirqPacket.o: src/DirqPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/DirqPacket.o src/DirqPacket.cpp
	
bin/DiscPacket.o: src/DiscPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/DiscPacket.o src/DiscPacket.cpp
	
bin/ErrorPacket.o: src/ErrorPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/ErrorPacket.o src/ErrorPacket.cpp
	
bin/LogRqPacket.o: src/LogRqPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/LogRqPacket.o src/LogRqPacket.cpp
	
bin/RrqPacket.o: src/RrqPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/RrqPacket.o src/RrqPacket.cpp
	
bin/WrqPacket.o: src/WrqPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/WrqPacket.o src/WrqPacket.cpp
	
bin/AckPacket.o: src/AckPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/AckPacket.o src/AckPacket.cpp
	
bin/RegPacket.o: src/RegPacket.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/RegPacket.o src/RegPacket.cpp


bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/EncoderDecoder.o: src/EncoderDecoder.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/EncoderDecoder.o src/EncoderDecoder.cpp

bin/MessagingProtocol.o: src/MessagingProtocol.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/MessagingProtocol.o src/MessagingProtocol.cpp

bin/Client.o: src/Client.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Client.o src/Client.cpp



#Clean the build directory
clean: 
	rm -f bin/*