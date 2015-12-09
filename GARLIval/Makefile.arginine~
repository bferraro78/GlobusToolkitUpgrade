SHELL = /bin/sh
CXX = g++
CXXFLAGS = -g -O2
CC = gcc

#CHANGE BASED ON MACHINE USED
BOINC_SRC_DIR=/export/work/boinc_source/boinc
MYSQL_INC_DIR=/usr/include/mysql


all: assimilator validator

#make our custom assimilator
assimilator: assimilator.o assimilate_handler.o validate_util.o 
	$(CXX) $(CXXFLAGS) -o lattice_assimilator assimilate_handler.o assimilator.o validate_util.o -L$(BOINC_SRC_DIR)/{,db,sched,lib} -L/usr/lib64/mysql -lsched -lboinc -lmysqlclient -lpthread

assimilator.o: 
	$(CXX) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o assimilator.o $(BOINC_SRC_DIR)/sched/assimilator.cpp 

assimilate_handler.o:
	$(CXX) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o assimilate_handler.o assimilate_handler.C


#make our custom validator
validator: 					\
	validator.o 			\
	garli_validator.o 	\
	validate_util.o			\
	validate_util2.o
	$(CXX) $(CXXFLAGS) -o garli_validator validator.o garli_validator.o validate_util.o validate_util2.o -L$(BOINC_SRC_DIR)/{,db,sched,lib} -L/usr/lib64/mysql -lsched -lboinc -lmysqlclient -lpthread

validator.o: $(BOINC_SRC_DIR)/sched/validator.cpp
	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	validator.o $(BOINC_SRC_DIR)/sched/validator.cpp

validate_util.o: $(BOINC_SRC_DIR)/sched/validate_util.o
	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR)  -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	validate_util.o $(BOINC_SRC_DIR)/sched/validate_util.cpp 


validate_util2.o: $(BOINC_SRC_DIR)/sched/validate_util2.o
	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR)  -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	validate_util2.o $(BOINC_SRC_DIR)/sched/validate_util2.cpp 


garli_validator.o:
	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	garli_validator.o garli_validator.C

#lattice_parse.o:
#	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	lattice_parse.o lattice_parse.C

#lattice_validate_util.o:
#	$(CXX) $(CXXFLAGS) -I$(MYSQL_INC_DIR) -I$(BOINC_SRC_DIR)/{,db,sched,lib} -c -o \
	lattice_validate_util.o lattice_validate_util.C

clean:
	rm *.o

install: 
	cp \
	lattice_assimilator garli_validator \
	cancel_job find_app get_assim_state \
	$(BOINC_LOCATION)/bin/
