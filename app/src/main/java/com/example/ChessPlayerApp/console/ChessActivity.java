package com.example.ChessPlayerApp.console;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ChessPlayerApp.R;

public class ChessActivity extends AppCompatActivity {

    TextView mConsole;
    ScrollView mScrollView;
    ImageButton[][] mChessBoard = new ImageButton[8][8];

    enum ChessState{
        Init,
        Pause,
        Action,
        Move
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        mConsole = findViewById(R.id.chess_console);
        mScrollView = findViewById(R.id.chess_console_scroll);


        JNI mJNI = new JNI();
        print(mJNI.printLogo());
        print(mJNI.printMenu());
        mJNI.newGame();
        print(mJNI.printSituation());
        print(mJNI.printBoard());
        mJNI.movePiece("a2", "a3");
        print(mJNI.printBoard());


    }

    private void initBoard(){
        for(int i = 0; i<8; i++)
            for(int j = 0; j<8; j++){
                String buttonID = (i + 1) + "" + ('a' + j);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                mChessBoard[i][j] = findViewById(resID);
            }
    }

    private void print(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConsole.append(msg);
                mScrollView.scrollTo(0, mConsole.getBottom());
            }
        });
    }
}
