#include <iostream>  
#include <boost/thread.hpp>  

class Task {
private:
	int _id;
public:
	Task (int number) : _id(number) {}

	void operator()(){
		for (int i= 0; i < 100; i++){
		   std::cout << i << ") Task " << _id << " is working" << std::endl; 
		}
		boost::this_thread::yield(); //Gives up the remainder of the current thread's time slice, to allow other threads to run. 
	}
};

int main(){
	Task task1(1);
	Task task2(2);
	
	boost::thread th1(task1); 
	boost::thread th2(task2); 
	th1.join();
	th2.join();	
	
	return 0;
}
