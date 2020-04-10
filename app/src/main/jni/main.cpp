#include "user_interface.h"
#include "game_control.h"


int main()
{
    GameController gameController = GameController();
    gameController.bRun = true;
    clearScreen();
    printLogo();

    string input = "";

    while(gameController.bRun)
    {
        printMenu();
        printMessage();

        // Get input from user
        cout << "Type here: ";
        getline(cin, input);
        if (input.length() != 1)
        {
            cout << "Invalid option. Type one letter only\n\n";
            continue;
        }
        try
        {
            switch (input[0])
            {
                case 'N':
                case 'n':
                {
                    gameController.newGame();
                    clearScreen();
                    printLogo();
                    printSituation(*gameController.current_game);
                    printBoard(*gameController.current_game);
                }
                break;

                case 'M':
                case 'm':
                {
                    if (NULL != gameController.current_game)
                    {
                        if ( gameController.current_game->isFinished() )
                        {
                            cout << "This game has already finished!\n";
                        }
                        else
                        {
                            gameController.movePiece();
                            //clearScreen();
                            printLogo();
                            printSituation( *gameController.current_game );
                            printBoard( *gameController.current_game );
                        }
                    }
                    else
                    {
                        cout << "No game running!\n";
                    }
                }
                break;

                case 'Q':
                case 'q':
                {
                    gameController.bRun = false;
                }
                break;

                case 'U':
                case 'u':
                {
                    if (NULL != gameController.current_game)
                    {
                        gameController.undoMove();
                        //clearScreen();
                        printLogo();
                        printSituation(*gameController.current_game);
                        printBoard(*gameController.current_game);
                    }
                    else
                    {
                        cout << "No game running\n";
                    }
                }
                break;

                case 'S':
                case 's':
                {
                    if (NULL != gameController.current_game)
                    {
                        gameController.saveGame();
                        clearScreen();
                        printLogo();
                        printSituation(*gameController.current_game);
                        printBoard(*gameController.current_game);
                    }
                    else
                    {
                        cout << "No game running\n";
                    }
                }
                break;

                case 'L':
                case 'l':
                {
                    gameController.loadGame();
                    clearScreen();
                    printLogo();
                    printSituation(*gameController.current_game);
                    printBoard(*gameController.current_game);
                }
                break;

                default:
                {
                    cout << "Option does not exist\n\n";
                }
                break;

            }

        }catch (const char* s)
        {
            s;
        }
    }

    return 0;
}