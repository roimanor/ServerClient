#include <iostream>
#include <boost/thread.hpp>
 
class Task{
private:
    int _id;
	boost::mutex * _mutex;
public:
    Task (int id, boost::mutex* mutex) : _id(id), _mutex(mutex) {}
	
    void run(){
        for (int i= 0; i < 100; i++){
			boost::mutex::scoped_lock lock(*_mutex);
			std::cout << i << ") Task " << _id << " is working" << std::endl; 
        }
    }
};
 
int main(){
    boost::mutex mutex;
    Task task1(1, &mutex);
    Task task2(2, &mutex);
	
	boost::thread th1(&Task::run, &task1); 
	boost::thread th2(&Task::run, &task2); 
	th1.join();
	th2.join();	
    return 0;
}