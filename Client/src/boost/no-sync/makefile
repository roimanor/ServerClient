C_FLAGS:=-c -Wall -Weffc++ -g -L/usr/local/include
L_DFLAGS:=-lboost_thread -lboost_system -L/usr/local/lib

all: bin/threads.o
	g++ -o bin/Threads bin/threads.o $(L_DFLAGS)

bin/threads.o: src/threads.cpp
	g++ $(C_FLAGS) -o bin/threads.o src/threads.cpp

.PHONY: clean
clean:
	rm -f bin/*
