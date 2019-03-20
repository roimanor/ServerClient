################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/AckPacket.cpp \
../src/BCastPacket.cpp \
../src/Client.cpp \
../src/ConnectionHandler.cpp \
../src/DataPacket.cpp \
../src/DelRqPacket.cpp \
../src/DirqPacket.cpp \
../src/DiscPacket.cpp \
../src/EncoderDecoder.cpp \
../src/ErrorPacket.cpp \
../src/LogRqPacket.cpp \
../src/MessagingProtocol.cpp \
../src/Packet.cpp \
../src/RrqPacket.cpp \
../src/WrqPacket.cpp 

OBJS += \
./src/AckPacket.o \
./src/BCastPacket.o \
./src/Client.o \
./src/ConnectionHandler.o \
./src/DataPacket.o \
./src/DelRqPacket.o \
./src/DirqPacket.o \
./src/DiscPacket.o \
./src/EncoderDecoder.o \
./src/ErrorPacket.o \
./src/LogRqPacket.o \
./src/MessagingProtocol.o \
./src/Packet.o \
./src/RrqPacket.o \
./src/WrqPacket.o 

CPP_DEPS += \
./src/AckPacket.d \
./src/BCastPacket.d \
./src/Client.d \
./src/ConnectionHandler.d \
./src/DataPacket.d \
./src/DelRqPacket.d \
./src/DirqPacket.d \
./src/DiscPacket.d \
./src/EncoderDecoder.d \
./src/ErrorPacket.d \
./src/LogRqPacket.d \
./src/MessagingProtocol.d \
./src/Packet.d \
./src/RrqPacket.d \
./src/WrqPacket.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


