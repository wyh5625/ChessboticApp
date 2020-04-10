CC=gcc
CXX=g++

BUILD_DIR = ./build

CFLAGS = -Wall -std=c++11

OBJS = main.o user_interface.o game_control.o chess.o

all: chess

chess: $(OBJS)
	$(CXX) $(CFLAGS) -o $(BUILD_DIR)/chess_console $(OBJS)

main.o: main.cpp

user_interface.o: user_interface.h user_interface.cpp

game_control.o: game_control.h game_control.cpp

chess.o: chess.h chess.cpp



clean:
	rm -f $(OBJS)

distclean:
	rm -f $(BUILD_DIR)

