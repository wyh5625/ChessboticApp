#include "includes.h"
#include "user_interface.h"
#include "../../../../../../../AppData/Local/Android/Sdk/ndk-bundle/sources/cxx-stl/llvm-libc++/include/iosfwd"


// Save the next message to be displayed (regardind last command)

string next_message;

//---------------------------------------------------------------------------------------
// User interface
// All the functions regarding the user interface are in this section
// Logo, Menu, Board, messages to the user
//---------------------------------------------------------------------------------------
void createNextMessage( string msg )
{
   next_message = msg;
}

void appendToNextMessage( string msg )
{
   next_message += msg;
}

string printLogo(void){
   appendToNextMessage("    ======================================\n");
   appendToNextMessage("       _____ _    _ ______  _____ _____\n");
   appendToNextMessage("      / ____| |  | |  ____|/ ____/ ____|\n");
   appendToNextMessage("     | |    | |__| | |__  | (___| (___ \n");
   appendToNextMessage("     | |    |  __  |  __|  \\___ \\\\___ \\ \n");
   appendToNextMessage("     | |____| |  | | |____ ____) |___) |\n");
   appendToNextMessage("      \\_____|_|  |_|______|_____/_____/\n\n");
   appendToNextMessage("    ======================================\n\n");
   return printMessage();
}

string printMenu(void)
{
    string msg = "Commands: (N)ew game\t(M)ove \t(U)ndo \t(S)ave \t(L)oad \t(Q)uit \n";
   return msg;
}

string printMessage(void)
{
   string msg = next_message;
   next_message = "";
   return msg;
}

string printLine(int iLine, int iColor1, int iColor2, Game& game)
{
   string msg = "";
   // Example (for CELL = 6):

   //  [6-char]
   //  |______| subline 1
   //  |___Q__| subline 2
   //  |______| subline 3

   // Define the CELL variable here. 
   // It represents how many horizontal characters will form one square
   // The number of vertical characters will be CELL/2
   // You can change it to alter the size of the board (an odd number will make the squares look rectangular)
   int CELL = 6;

   // Since the width of the characters BLACK and WHITE is half of the height,
   // we need to use two characters in a row.
   // So if we have CELL characters, we must have CELL/2 sublines
   for (int subLine = 0; subLine < CELL/2; subLine++)
   {
      // A sub-line is consisted of 8 cells, but we can group it
      // in 4 iPairs of black&white
      for (int iPair = 0; iPair < 4; iPair++)
      {
         // First cell of the pair
         for (int subColumn = 0; subColumn < CELL; subColumn++)
         {
            // The piece should be in the "middle" of the cell
            // For 3 sub-lines, in sub-line 1
            // For 6 sub-columns, sub-column 3
            if ( subLine == 1 && subColumn == 3)
            {
               msg += char(game.getPieceAtPosition(iLine, iPair*2) != 0x20 ? game.getPieceAtPosition(iLine, iPair*2) : iColor1);
            }
            else
            {
               msg += char(iColor1);
            }
         }

         // Second cell of the pair
         for (int subColumn = 0; subColumn < CELL; subColumn++)
         {
            // The piece should be in the "middle" of the cell
            // For 3 sub-lines, in sub-line 1
            // For 6 sub-columns, sub-column 3
            if ( subLine == 1 && subColumn == 3)
            {
               msg += char(game.getPieceAtPosition(iLine,iPair*2+1) != 0x20 ? game.getPieceAtPosition(iLine,iPair*2+1) : iColor2);
            }
            else
            {
               msg += char(iColor2);
            }
         }
      }

      // Write the number of the line on the right
      if ( 1 == subLine )
      {
         msg = msg + string("   ") + to_string(iLine+1);
      }

      msg += string("\n");

   }
    appendToNextMessage(msg);
   return printMessage();
}

string printSituation(Game& game)
{
   // Last moves - print only if at least one movePiece has been made
   if ( 0 != game.rounds.size() )
   {
      appendToNextMessage("Last moves:\n");
      int iMoves = game.rounds.size();
      int iToShow = iMoves >= 5 ? 5 : iMoves;

      string space = "";
      while( iToShow-- )
      {
         if ( iMoves < 10 )
         {
            // Add an extra hardspace to allign the numbers that are smaller than 10
            space = " ";
         }
      
         string msg = string(space) + to_string(iMoves) + string(" ..... ") +  game.rounds[iMoves-1].white_move.c_str() + string(" | ") + game.rounds[iMoves - 1].black_move.c_str() + string("\n");
         appendToNextMessage(msg);
         iMoves--;
      }

      appendToNextMessage("\n");
   }

   // Captured pieces - print only if at least one piece has been captured
   if ( 0 != game.white_captured.size() || 0 != game.black_captured.size() )
   {
      string msg = "";
      msg += string("---------------------------------------------\n");
      msg += string("WHITE captured: ");
      for (unsigned i = 0; i < game.white_captured.size(); i++)
      {
         msg = msg + char(game.white_captured[i]) + string(" ");
      }
      msg += string("\n");

      msg += string("black captured: ");
      for (unsigned i = 0; i < game.black_captured.size(); i++)
      {
         msg = msg + char(game.black_captured[i]) + string(" ");
      }
      msg += string("\n");

      msg += string("---------------------------------------------\n");
      appendToNextMessage(msg);
   }

   // Current turn
   string msg = string("Current turn: ") + string((game.getCurrentTurn() == Chess::WHITE_PIECE ? "WHITE (upper case)" : "BLACK (lower case)")) + string("\n\n");
   appendToNextMessage(msg);
   return printMessage();
}

string printBoard(Game& game)
{
   string msg = "";
   msg += string("   A     B     C     D     E     F     G     H\n");

   for (int iLine = 7; iLine >= 0; iLine--)
   {
      if ( iLine%2 == 0)
      {
         // Line starting with BLACK
         msg += printLine(iLine, BLACK_SQUARE, WHITE_SQUARE, game);
      }
      else
      {
         // Line starting with WHITE
         msg += printLine(iLine, WHITE_SQUARE, BLACK_SQUARE, game);
      }
   }

   appendToNextMessage(msg);
   return printMessage();
}