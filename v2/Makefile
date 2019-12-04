
CC=gcc
CFLAGS=-Wall
DEPS = ripNet.h

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

default: encoder 

encoder: encoder.o
	$(CC) -o encoder encoder.o

test:
	./encoder ../bobby_tables.png ../in

clean: 
	rm *.o
	rm encoder