package it.univpm.pernini.corsoiot_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText myEditText;
    Button myButton;
    TextView myTextView;

    Button newActivityButton;
    Button btActivityButton;

    String STATUS_TEXT="status_text";
    String myText;

    String STATUS_COUNTER="status_counter";
    int counter=0;

    String DEBUG="lifecycledebug";

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showToast("onCreate!");

        myEditText =(EditText)findViewById(R.id.editText);
        myButton =(Button)findViewById(R.id.button);
        myTextView=(TextView)findViewById(R.id.textView);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                myTextView.setText(Integer.toString(counter));
            }
        });


        newActivityButton=(Button)findViewById(R.id.button2);
        newActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });

        btActivityButton=(Button)findViewById(R.id.button3);
        btActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, BTActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG,"onStart");
        showToast("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(DEBUG,"onRestart");
        showToast("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG,"onResume");
        showToast("onResume");

        getPreferences();

        myTextView.setText(Integer.toString(counter));
        myEditText.setText(myText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG,"onPause");
        showToast("onPause");
        savePreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG,"onStop");
        showToast("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG,"onDestroy");
        showToast("onDestroy");
    }

    @Override
    public void onBackPressed() {
        showToast("onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG,"onSaveInstanceState");
        myText=myEditText.getText().toString();
        Log.d(DEBUG, myText);
        outState.putString(STATUS_TEXT, myText);
        outState.putInt(STATUS_COUNTER, counter);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(DEBUG, "onRestoreInstanceState");
        myText = savedInstanceState.getString(STATUS_TEXT);
        myEditText.setText(myText);
        counter=savedInstanceState.getInt(STATUS_COUNTER);
        myTextView.setText(Integer.toString(counter));
    }

    void getPreferences()
    {
        counter = sharedpreferences.getInt("counter", 0);
        myText = sharedpreferences.getString("text", "Enter Text");
    }

    void savePreferences()
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("counter", counter);
        myText=myEditText.getText().toString();
        editor.putString("text", myText);
        editor.commit();
    }



    void showToast(String texttoshow) {
//        Context context = getApplicationContext();
//        CharSequence text = texttoshow;
//        int duration = Toast.LENGTH_SHORT;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();
    }

}
