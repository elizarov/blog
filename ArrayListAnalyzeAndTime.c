#include "ArrayListAnalyzeAndTime.h"

/*
 * Class:     ArrayListAnalyzeAndTime
 * Method:    getCurrentObjectAddress
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_ArrayListAnalyzeAndTime_getCurrentObjectAddress(JNIEnv *evn, jclass cls, jobject obj)
{
	// we know that jobject is a pointer to oop in JVM, where oop is Ordinary Object Pointer
	return (jlong) *((void**)obj);
}

#if defined _WIN32 || defined _WIN64

#include <windows.h>

// Define our own DLL entry point, so that we can link Windows DLL wihtout C runtime library (which we don't need)
BOOL WINAPI DllEntryPoint(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpReserved)
{
	return TRUE;
}

#endif
