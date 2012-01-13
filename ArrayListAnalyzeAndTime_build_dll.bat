SET JAVA_HOME=C:\Java\jdk1.6.0_20
cl ArrayListAnalyzeAndTime.c /O1 /I%JAVA_HOME%\include\ /I%JAVA_HOME%\include\win32 /LD /link /NODEFAULTLIB /ENTRY:DllEntryPoint
