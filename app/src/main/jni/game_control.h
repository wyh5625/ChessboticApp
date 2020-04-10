#pragma once
#include "chess.h"
class GameController
{
    public: 
        Game* current_game;
        bool bRun;

        GameController();
        void newGame(void);
        void undoMove(void);
        bool movePiece(string from, string to);
        void saveGame(void);
        void loadGame(void);
    private:
        bool isMoveValid(Chess::Position present, Chess::Position future, Chess::EnPassant* S_enPassant, Chess::Castling* S_castling, Chess::Promotion* S_promotion);
        void makeTheMove(Chess::Position present, Chess::Position future, Chess::EnPassant* S_enPassant, Chess::Castling* S_castling, Chess::Promotion* S_promotion);
        
};
