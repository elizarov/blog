#include <windows.h>
#include <stdio.h>

#include <vector>

using namespace std;

__int32 dummy; // to avoid optimizing away our computations

class IntVectorIterationTiming {
private:
	vector<__int32> vec;
	__int32 runIteration();
public:
	IntVectorIterationTiming(int size);
	double time();
};

IntVectorIterationTiming::IntVectorIterationTiming(int size) {
	__int64 multiplier = 0x5DEECE66DL;
	__int64 mask = ((__int64)1 << 48) - 1;
	__int64 seed = (1 ^ multiplier) & mask;
	for (int i = 0; i < size; i++) {
		seed = (seed * multiplier + 0xBL) & mask;
		vec.push_back((__int32)(seed >> 16));
	}
}

__int32 IntVectorIterationTiming::runIteration() {
	__int32 sum = 0;
	for (int i = 0, n = vec.size(); i < n; i++)
		sum += vec[i];
	return sum;
}

double IntVectorIterationTiming::time() {
	int reps = 1000000000 / vec.size();
	__int64 start, finish, freq;
	QueryPerformanceCounter((LARGE_INTEGER*)&start);
	for (int rep = 0; rep < reps; rep++)
		dummy += runIteration();
	QueryPerformanceCounter((LARGE_INTEGER*)&finish);
	QueryPerformanceFrequency((LARGE_INTEGER*)&freq);
	return (double)(finish - start) * 1000000000 / freq / reps / vec.size();		
}

int main(int argc, const char* argv[]) {
	for (int pass = 1; pass <= 5; pass++) { // let CPU warm up...
		printf("----- PASS %d -----\n", pass);
		for (int size = 1000; size <= 10000000; size *= 10) {
			dummy = 0;
			IntVectorIterationTiming* timing = new IntVectorIterationTiming(size);
			double time = timing->time();
			printf("[%8d]: %.2f ns per item (%d)\n", size, time, dummy);
		}
	}
}
