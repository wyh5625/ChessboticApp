#include "game_control.h"
#include "user_interface.h"
#include "chess.h"

GameController::GameController()
{
    current_game = NULL;
    bRun = false;
}

bool GameController::isMoveValid(Chess::Position present, Chess::Position future, Chess::EnPassant* S_enPassant, Chess::Castling* S_castling, Chess::Promotion* S_promotion)
{
    bool bValid = false;

   char chPiece = current_game->getPieceAtPosition(present.iRow, present.iColumn);

   // ----------------------------------------------------
   // 1. Is the piece  allowed to movePiece in that direction?
   // ----------------------------------------------------
   switch( toupper(chPiece) )
   {
      case 'P':
      {
         // Wants to movePiece forward
         if ( future.iColumn == present.iColumn )
         {
            // Simple movePiece forward
            if ( (Chess::isWhitePiece(chPiece) && future.iRow == present.iRow + 1) ||
                 (Chess::isBlackPiece(chPiece) && future.iRow == present.iRow - 1) )
            {
               if ( EMPTY_SQUARE == current_game->getPieceAtPosition(future.iRow, future.iColumn) )
               {
                  bValid = true;
               }
            }

            // Double movePiece forward
            else if ( (Chess::isWhitePiece(chPiece) && future.iRow == present.iRow + 2) ||
                      (Chess::isBlackPiece(chPiece) && future.iRow == present.iRow - 2) )
            {
               // This is only allowed if the pawn is in its original place
               if ( Chess::isWhitePiece(chPiece) )
               {
                  if ( EMPTY_SQUARE == current_game->getPieceAtPosition(future.iRow-1, future.iColumn) &&
                       EMPTY_SQUARE == current_game->getPieceAtPosition(future.iRow, future.iColumn)   &&
                                1   == present.iRow )
                  {
                     bValid = true;
                  }
               }
               else // if ( isBlackPiece(chPiece) )
               {
                  if ( EMPTY_SQUARE == current_game->getPieceAtPosition(future.iRow + 1, future.iColumn) &&
                       EMPTY_SQUARE == current_game->getPieceAtPosition(future.iRow, future.iColumn)     &&
                                6   == present.iRow)
                  {
                     bValid = true;
                  }
               }
            }
            else
            {
               // This is invalid
               return false;
            }
         }
         
         // The "en passant" movePiece
         else if ( (Chess::isWhitePiece(chPiece) && 4 == present.iRow && 5 == future.iRow && 1 == abs(future.iColumn - present.iColumn) ) ||
                   (Chess::isBlackPiece(chPiece) && 3 == present.iRow && 2 == future.iRow && 1 == abs(future.iColumn - present.iColumn) ) )
         {
            // It is only valid if last movePiece of the opponent was a double movePiece forward by a pawn on a adjacent column
            string last_move = current_game->getLastMove();

            // Parse the line
            Chess::Position LastMoveFrom;
            Chess::Position LastMoveTo;
            current_game->parseMove(last_move, &LastMoveFrom, &LastMoveTo);

            // First of all, was it a pawn?
            char chLstMvPiece = current_game->getPieceAtPosition(LastMoveTo.iRow, LastMoveTo.iColumn);

            if (toupper(chLstMvPiece) != 'P')
            {
               return false;
            }

            // Did the pawn have a double movePiece forward and was it an adjacent column?
            if ( 2 == abs(LastMoveTo.iRow - LastMoveFrom.iRow) && 1 == abs(LastMoveFrom.iColumn - present.iColumn) )
            {
               cout << "En passant movePiece!\n";
               bValid = true;

               S_enPassant->bApplied = true;
               S_enPassant->PawnCaptured.iRow    = LastMoveTo.iRow;
               S_enPassant->PawnCaptured.iColumn = LastMoveTo.iColumn;
            }
         }

         // Wants to capture a piece
         else if (1 == abs(future.iColumn - present.iColumn))
         {
            if ( (Chess::isWhitePiece(chPiece) && future.iRow == present.iRow + 1) || (Chess::isBlackPiece(chPiece) && future.iRow == present.iRow - 1))
            {
               // Only allowed if there is something to be captured in the square
               if (EMPTY_SQUARE != current_game->getPieceAtPosition(future.iRow, future.iColumn))
               {
                  bValid = true;
                  cout << "Pawn captured a piece!\n";
               }
            }
         }
         else
         {
            // This is invalid
            return false;
         }

         // If a pawn reaches its eight rank, it must be promoted to another piece
         if ( (Chess::isWhitePiece( chPiece ) && 7 == future.iRow) ||
              (Chess::isBlackPiece( chPiece ) && 0 == future.iRow) )
         {
            cout << "Pawn must be promoted!\n";
            S_promotion->bApplied = true;
         }
      }
      break;

      case 'R':
      {
         // Horizontal movePiece
         if ( (future.iRow == present.iRow) && (future.iColumn != present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::HORIZONTAL) )
            {
               bValid = true;
            }
         }
         // Vertical movePiece
         else if ( (future.iRow != present.iRow) && (future.iColumn == present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::VERTICAL) )
            {
               bValid = true;
            }
         }
      }
      break;

      case 'N':
      {
         if ( (2 == abs(future.iRow - present.iRow)) && (1 == abs(future.iColumn - present.iColumn)) )
         {
            bValid = true;
         }

         else if (( 1 == abs(future.iRow - present.iRow)) && (2 == abs(future.iColumn - present.iColumn)) )
         {
            bValid = true;
         }
      }
      break;

      case 'B':
      {
         // Diagonal movePiece
         if ( abs(future.iRow - present.iRow) == abs(future.iColumn - present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::DIAGONAL) )
            {
               bValid = true;
            }
         }
      }
      break;

      case 'Q':
      {
         // Horizontal movePiece
         if ( (future.iRow == present.iRow) && (future.iColumn != present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::HORIZONTAL))
            {
               bValid = true;
            }
         }
         // Vertical movePiece
         else if ( (future.iRow != present.iRow) && (future.iColumn == present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::VERTICAL))
            {
               bValid = true;
            }
         }

         // Diagonal movePiece
         else if ( abs(future.iRow - present.iRow) == abs(future.iColumn - present.iColumn) )
         {
            // Check if there are no pieces on the way
            if ( current_game->isPathFree(present, future, Chess::DIAGONAL))
            {
               bValid = true;
            }
         }
      }
      break;

      case 'K':
      {
         // Horizontal movePiece by 1
         if ( (future.iRow == present.iRow) && (1 == abs(future.iColumn - present.iColumn) ) )
         {
            bValid = true;
         }

         // Vertical movePiece by 1
         else if ( (future.iColumn == present.iColumn) && (1 == abs(future.iRow - present.iRow) ) )
         {
            bValid = true;
         }

         // Diagonal movePiece by 1
         else if ( (1 == abs(future.iRow - present.iRow) ) && (1 == abs(future.iColumn - present.iColumn) ) )
         {
            bValid = true;
         }

         // Castling
         else if ( (future.iRow == present.iRow) && (2 == abs(future.iColumn - present.iColumn) ) )
         {
            // Castling is only allowed in these circunstances:

            // 1. King is not in check
            if ( true == current_game->playerKingInCheck() )
            {
               return false;
            }

            // 2. No pieces in between the king and the rook
            if ( false == current_game->isPathFree( present, future, Chess::HORIZONTAL ) )
            {
               return false;
            }

            // 3. King and rook must not have moved yet;
            // 4. King must not pass through a square that is attacked by an enemy piece
            if ( future.iColumn > present.iColumn )
            {
               // if future.iColumn is greather, it means king side
               if ( false == current_game->castlingAllowed(Chess::Side::KING_SIDE, Chess::getPieceColor(chPiece) ) )
               {
                  createNextMessage("Castling to the king side is not allowed.\n");
                  return false;
               }
               else
               {
                  // Check if the square that the king skips is not under attack
                  Chess::UnderAttack square_skipped = current_game->isUnderAttack( present.iRow, present.iColumn + 1, current_game->getCurrentTurn() );
                  if ( false == square_skipped.bUnderAttack )
                  {
                     // Fill the S_castling structure
                     S_castling->bApplied = true;

                     // Present position of the rook
                     S_castling->rook_before.iRow    = present.iRow;
                     S_castling->rook_before.iColumn = present.iColumn + 3;

                     // Future position of the rook
                     S_castling->rook_after.iRow    = future.iRow;
                     S_castling->rook_after.iColumn = present.iColumn + 1; // future.iColumn -1

                     bValid = true;
                  }
               }
            }
            else //if (future.iColumn < present.iColumn)
            {
               // if present.iColumn is greather, it means queen side
               if (false == current_game->castlingAllowed(Chess::Side::QUEEN_SIDE, Chess::getPieceColor(chPiece)))
               {
                  createNextMessage("Castling to the queen side is not allowed.\n");
                  return false;
               }
               else
               {
                  // Check if the square that the king skips is not attacked
                  Chess::UnderAttack square_skipped = current_game->isUnderAttack( present.iRow, present.iColumn - 1, current_game->getCurrentTurn() );
                  if ( false == square_skipped.bUnderAttack )
                  {
                     // Fill the S_castling structure
                     S_castling->bApplied = true;

                     // Present position of the rook
                     S_castling->rook_before.iRow    = present.iRow;
                     S_castling->rook_before.iColumn = present.iColumn - 4;

                     // Future position of the rook
                     S_castling->rook_after.iRow    = future.iRow;
                     S_castling->rook_after.iColumn = present.iColumn - 1; // future.iColumn +1

                     bValid = true;
                  }
               }
            }
         }
      }
      break;

      default:
      {
         cout << "!!!!Should not reach here. Invalid piece: " << char(chPiece) << "\n\n\n";
      }
      break;
   }

   // If it is a movePiece in an invalid direction, do not even bother to check the rest
   if ( false == bValid )
   {
      cout << "Piece is not allowed to movePiece to that square\n";
      return false;
   }


   // -------------------------------------------------------------------------
   // 2. Is there another piece of the same color on the destination square?
   // -------------------------------------------------------------------------
   if (current_game->isSquareOccupied(future.iRow, future.iColumn))
   {
      char chAuxPiece = current_game->getPieceAtPosition(future.iRow, future.iColumn);
      if ( Chess::getPieceColor(chPiece) == Chess::getPieceColor(chAuxPiece) )
      {
         cout << "Position is already taken by a piece of the same color\n";
         return false;
      }
   }

   // ----------------------------------------------
   // 3. Would the king be in check after the movePiece?
   // ----------------------------------------------
   if ( true == current_game->wouldKingBeInCheck(chPiece, present, future, S_enPassant) )
   {
      cout << "Move would put player's king in check\n";
      return false;
   }

   return bValid;
}

void GameController::makeTheMove(Chess::Position present, Chess::Position future, Chess::EnPassant* S_enPassant, Chess::Castling* S_castling, Chess::Promotion* S_promotion)
{
    char chPiece = current_game->getPieceAtPosition(present.iRow, present.iColumn);

   // -----------------------
   // Captured a piece?
   // -----------------------
   if ( current_game->isSquareOccupied(future.iRow, future.iColumn) )
   {
      char chAuxPiece = current_game->getPieceAtPosition(future.iRow, future.iColumn);

      if ( Chess::getPieceColor(chPiece) != Chess::getPieceColor(chAuxPiece))
      {
         createNextMessage(Chess::describePiece(chAuxPiece) + " captured!\n");
      }
      else
      {
         cout << "Error. We should not be making this movePiece\n";
         throw("Error. We should not be making this movePiece");
      }
   }
   else if (true == S_enPassant->bApplied)
   {
      createNextMessage("Pawn captured by \"en passant\" movePiece!\n");
   }

   if ( (true == S_castling->bApplied) )
   {
      createNextMessage("Castling applied!\n");
   }

   current_game->movePiece(present, future, S_enPassant, S_castling, S_promotion);
}

//---------------------------------------------------------------------------------------
// Commands
// Functions to handle the commands of the program
// New game, Move, Undo, Save game, Load game, etc
//---------------------------------------------------------------------------------------

void GameController::newGame(void)
{
    if (NULL != current_game)
   {
      delete current_game;
   }

   current_game = new Game();
}

void GameController::undoMove(void)
{
   if ( false == current_game->undoIsPossible() )
   {
      createNextMessage("Undo is not possible now!\n");
      return;
   }

   current_game->undoLastMove();
   createNextMessage("Last movePiece was undone\n");
}

bool GameController::movePiece(string from, string to)
{
   std::string to_record;

   Chess::Position present;
   present.iColumn = from[0];
   present.iRow    = from[1];

   // ---------------------------------------------------
   // Did the user pick a valid piece?
   // Must check if:
   // - It's inside the board (A1-H8)
   // - There is a piece in the square
   // - The piece is consistent with the player's turn
   // ---------------------------------------------------
   present.iColumn = toupper(present.iColumn);

   if ( present.iColumn < 'A' || present.iColumn > 'H' )
   {
      return false;
   }

   if ( present.iRow < '0' || present.iRow > '8' )
   {
      return false;
   }

   // Put in the string to be logged
   to_record += present.iColumn;
   to_record += present.iRow;
   to_record += "-";

   // Convert column from ['A'-'H'] to [0x00-0x07]
   present.iColumn = present.iColumn - 'A';

   // Convert row from ['1'-'8'] to [0x00-0x07]
   present.iRow  = present.iRow  - '1';

   char chPiece = current_game->getPieceAtPosition(present.iRow, present.iColumn);

   if ( 0x20 == chPiece )
   {
      return false;
   }

   if ( Chess::WHITE_PIECE == current_game->getCurrentTurn() )
   {
      if ( false == Chess::isWhitePiece(chPiece) )
      {
         //createNextMessage("It is WHITE's turn and you picked a BLACK piece\n");
         return false;
      }
   }
   else
   {
      if ( false == Chess::isBlackPiece(chPiece) )
      {
         //createNextMessage("It is BLACK's turn and you picked a WHITE piece\n");
         return false;
      }
   }

   // ---------------------------------------------------
   // Get user input for the square to movePiece to
   // ---------------------------------------------------

   // ---------------------------------------------------
   // Did the user pick a valid house to movePiece?
   // Must check if:
   // - It's inside the board (A1-H8)
   // - The movePiece is valid
   // ---------------------------------------------------
   Chess::Position future;
   future.iColumn = to[0];
   future.iRow    = to[1];

   future.iColumn = toupper(future.iColumn);

   if ( future.iColumn < 'A' || future.iColumn > 'H' )
   {
      //createNextMessage("Invalid column.\n");
      return false;
   }

   if ( future.iRow < '0' || future.iRow > '8' )
   {
      //createNextMessage("Invalid row.\n");
      return false;
   }

   // Put in the string to be logged
   to_record += future.iColumn;
   to_record += future.iRow;

   // Convert columns from ['A'-'H'] to [0x00-0x07]
   future.iColumn = future.iColumn - 'A';

   // Convert row from ['1'-'8'] to [0x00-0x07]
   future.iRow = future.iRow - '1';

   // Check if it is not the exact same square
   if ( future.iRow == present.iRow && future.iColumn == present.iColumn )
   {
      //createNextMessage("[Invalid] You picked the same square!\n");
      return false;
   }

   // Is that movePiece allowed?
   Chess::EnPassant  S_enPassant  = { 0 };
   Chess::Castling   S_castling   = { 0 };
   Chess::Promotion  S_promotion  = { 0 };

   if ( false == isMoveValid(present, future, &S_enPassant, &S_castling, &S_promotion) )
   {
      //createNextMessage("[Invalid] Piece can not movePiece to that square!\n");
      return false;
   }
   
   // ---------------------------------------------------
   // Promotion: user most choose a piece to
   // replace the pawn
   // ---------------------------------------------------
   /*
   if ( S_promotion.bApplied == true )
   {
      cout << "Promote to (Q, R, N, B): ";
      std::string piece;
      getline(cin, piece);

      if ( piece.length() > 1 )
      {
         createNextMessage("You should type only one character (Q, R, N or B)\n");
         return;
      }

      char chPromoted = toupper(piece[0]);

      if ( chPromoted != 'Q' && chPromoted != 'R' && chPromoted != 'N' && chPromoted != 'B' )
      {
         createNextMessage("Invalid character.\n");
         return;
      }

      S_promotion.chBefore = current_game->getPieceAtPosition(present.iRow, present.iColumn);

      if (Chess::WHITE_PLAYER == current_game->getCurrentTurn())
      {
         S_promotion.chAfter = toupper(chPromoted);
      }
      else
      {
         S_promotion.chAfter = tolower(chPromoted);
      }

      to_record += '=';
      to_record += toupper(chPromoted); // always log with a capital letter
   }
   */

   // ---------------------------------------------------
   // Log the movePiece: do it prior to making the movePiece
   // because we need the getCurrentTurn()
   // ---------------------------------------------------
   current_game->logMove( to_record );

   // ---------------------------------------------------
   // Make the movePiece
   // ---------------------------------------------------
   makeTheMove(present, future, &S_enPassant, &S_castling, &S_promotion);

   // ---------------------------------------------------------------
   // Check if this movePiece we just did put the oponent's king in check
   // Keep in mind that player turn has already changed
   // ---------------------------------------------------------------
   if ( true == current_game->playerKingInCheck() )
   {
      if (true == current_game->isCheckMate() )
      {
         if (Chess::WHITE_PLAYER == current_game->getCurrentTurn())
         {
            appendToNextMessage("Checkmate! Black wins the game!\n");
         }
         else
         {
            appendToNextMessage("Checkmate! White wins the game!\n");
         }
      }
      else
      { 
         // Add to the string with '+=' because it's possible that
         // there is already one message (e.g., piece captured)
         if (Chess::WHITE_PLAYER == current_game->getCurrentTurn())
         {
            appendToNextMessage("White king is in check!\n");
         }
         else
         {
            appendToNextMessage("Black king is in check!\n");
         }
      }
   }

   return true;
}

void GameController::saveGame(void)
{
   string file_name;
   cout << "Type file name to be saved (no extension): ";

   getline(cin, file_name);
   file_name += ".dat";

   std::ofstream ofs(file_name);
   if (ofs.is_open())
   {
      // Write the date and time of save operation
      auto time_now = std::chrono::system_clock::now();
      std::time_t end_time = std::chrono::system_clock::to_time_t(time_now);
      ofs << "[Chess console] Saved at: " << std::ctime(&end_time);

      // Write the moves
      for (unsigned i = 0; i < current_game->rounds.size(); i++)
      {
         ofs << current_game->rounds[i].white_move.c_str() << " | " << current_game->rounds[i].black_move.c_str() << "\n";
      }

      ofs.close();
      createNextMessage("Game saved as " + file_name + "\n");
   }
   else
   {
      cout << "Error creating file! Save failed\n";
   }

   return;
}

void GameController::loadGame(void)
{
   string file_name;
   cout << "Type file name to be loaded (no extension): ";

   getline(cin, file_name);
   file_name += ".dat";

   std::ifstream ifs(file_name);

   if (ifs)
   {
      // First, reset the pieces
      if (NULL != current_game)
      {
         delete current_game;
      }

      current_game = new Game();

      // Now, read the lines from the file and then make the moves
      std::string line;

      while (std::getline(ifs, line) )
      {
         // Skip lines that starts with "[]"
         if ( 0 == line.compare(0, 1, "["))
         {
            continue;
         }

         // There might be one or two moves in the line
         string loaded_move[2];
         
         // Find the separator and subtract one
         std::size_t separator = line.find(" |");

         // For the first movePiece, read from the beginning of the string until the separator
         loaded_move[0] = line.substr(0, separator);

         // For the second movePiece, read from the separator until the end of the string (omit second parameter)
         loaded_move[1] = line.substr(line.find("|") + 2);

         for (int i = 0; i < 2 && loaded_move[i] != ""; i++)
         {
            // Parse the line
            Chess::Position from;
            Chess::Position to;

            char chPromoted = 0;

            current_game->parseMove(loaded_move[i], &from, &to, &chPromoted);

            // Check if line is valid
            if ( from.iColumn < 0 || from.iColumn > 7 ||
                 from.iRow    < 0 || from.iRow    > 7 ||
                 to.iColumn   < 0 || to.iColumn   > 7 ||
                 to.iRow      < 0 || to.iRow      > 7 )
            {
               createNextMessage("[Invalid] Can't load this game because there are invalid lines!\n");

               // Clear everything and return
               current_game = new Game();
               return;
            }

            // Is that movePiece allowed? (should be because we already validated before saving)
            Chess::EnPassant S_enPassant = { 0 };
            Chess::Castling  S_castling  = { 0 };
            Chess::Promotion S_promotion = { 0 };

            if ( false == isMoveValid(from, to, &S_enPassant, &S_castling, &S_promotion) )
            {
               createNextMessage("[Invalid] Can't load this game because there are invalid moves!\n");

               // Clear everything and return
               current_game = new Game();
               return;
            }

            // ---------------------------------------------------
            // A promotion occurred
            // ---------------------------------------------------
            if ( S_promotion.bApplied == true )
            {
               if ( chPromoted != 'Q' && chPromoted != 'R' && chPromoted != 'N' && chPromoted != 'B' )
               {
                  createNextMessage("[Invalid] Can't load this game because there is an invalid promotion!\n");

                  // Clear everything and return
                  current_game = new Game();
                  return;
               }

               S_promotion.chBefore = current_game->getPieceAtPosition(from.iRow, from.iColumn);

               if (Chess::WHITE_PLAYER == current_game->getCurrentTurn())
               {
                  S_promotion.chAfter = toupper(chPromoted);
               }
               else
               {
                  S_promotion.chAfter = tolower(chPromoted);
               }
            }


            // Log the movePiece
            current_game->logMove(loaded_move[i]);

            // Make the movePiece
            makeTheMove(from, to, &S_enPassant, &S_castling, &S_promotion);
         }
      }

      // Extra line after the user input
      createNextMessage("Game loaded from " + file_name + "\n");

      return;
   }
   else
   {
      createNextMessage("Error loading " + file_name + ". Creating a new game instead\n");
      current_game = new Game();
      return;
   }
}