CC=g++
CFLAGS=-I/usr/include/mysql -I/usr/include/libxml2 -L/usr/lib64/mysql -lmysqlclient -lxml2

all: getBoincJobStatus

getBoincJobStatus: getBoincJobStatus.cpp
	$(CC) $(CFLAGS) -o getBoincJobStatus getBoincJobStatus.cpp
