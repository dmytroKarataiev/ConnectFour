package karataiev.dmytro.connectfour;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class NewGame extends AppCompatActivity {

    Button newGame, vsRandom, vsPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        addListenerOnButton();
    }

    // button initialization (vsEasy, vsRandom, vsPlayer)
    public void addListenerOnButton() {

        final Context context = this;
        newGame = (Button) findViewById(R.id.newGameScreen);
        newGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("player", 1);
                startActivity(intent);

            }

        });

        vsRandom = (Button) findViewById(R.id.vsRandom);
        vsRandom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("player", 2);
                startActivity(intent);

            }

        });

        vsPlayer = (Button) findViewById(R.id.vsPlayer);
        vsPlayer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("player", 3);
                startActivity(intent);

            }

        });

    }

    /**
     *  check if back button was pressed and confirm exiting
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
