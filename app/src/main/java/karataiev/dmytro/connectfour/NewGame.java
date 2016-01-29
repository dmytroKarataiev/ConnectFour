package karataiev.dmytro.connectfour;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import skeleton.SkeletonActivity;


public class NewGame extends AppCompatActivity {

    Button newGame, vsRandom, vsPlayer, vsHard, vsAdvanced, skeleton;

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

        vsAdvanced = (Button) findViewById(R.id.vsAdvanced);
        vsAdvanced.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("player", 4);
                startActivity(intent);
            }

        });

        vsHard = (Button) findViewById(R.id.vsHard);
        vsHard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("player", 5);
                startActivity(intent);
            }

        });

        skeleton = (Button) findViewById(R.id.multiplayer);
        skeleton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SkeletonActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
