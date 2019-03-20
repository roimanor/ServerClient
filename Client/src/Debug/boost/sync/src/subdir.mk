################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../boost/sync/src/threads.cpp 

OBJS += \
./boost/sync/src/threads.o 

CPP_DEPS += \
./boost/sync/src/threads.d 


# Each subdirectory must supply rules for building sources it contributes
boost/sync/src/%.o: ../boost/sync/src/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


