#pragma once
#include "chess.h"

#define WHITE_SQUARE 0x20
#define BLACK_SQUARE 0x20
#define EMPTY_SQUARE 0x20

void createNextMessage( string msg );
void appendToNextMessage( string msg );
void clearScreen( void );
string printLogo( void );
string printMenu( void );
string printMessage( void );
string printLine( int iLine, int iColor1, int iColor2, Game& game );
string printSituation( Game& game );
string printBoard(Game& game);