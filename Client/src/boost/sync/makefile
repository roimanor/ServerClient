C_FLAGS:=-c -Wall -Weffc++ -g -L/usr/local/include
L_DFLAGS:=-lboost_thread -lboost_system -L/usr/local/lib

all: bin/threads.o
	g++ bin/threads.o -o bin/Threads $(L_DFLAGS)

bin/threads.o: src/threads.cpp
	g++ $(C_FLAGS) src/threads.cpp -o bin/threads.o

.PHONY: clean
clean:
	rm -f bin/*
