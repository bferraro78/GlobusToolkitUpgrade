CXX=g++
CPPFLAGS=-I/usr/include/mysql -I/usr/include/libxml2
LDFLAGS=-L/usr/lib64/mysql
LDLIBS=-lmysqlclient -lxml2

all: getBoincJobStatus

getBoincJobStatus: getBoincJobStatus.cpp
	$(CXX) $(CPPFLAGS) $(LDFLAGS) $(LDLIBS) -o getBoincJobStatus \
		getBoincJobStatus.cpp

clean:
	rm getBoincJobStatus
