package com.example.guesstheceleb;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    ArrayList<String> answers = new ArrayList<String>();
    int chosenCeleb = 0, correct;
    ImageView celebImageView;
    Button button1, button2, button3, button4;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(is);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";

            try {
                URL url = new URL(strings[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream is = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(is);

                int data = reader.read();

                while ((data != -1)){
                     char current = (char) data;
                     result += current;
                     data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void checking(View view){
        if(view.getTag().toString().equals(String.valueOf(correct))){
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Wrong! It was "+celebNames.get(chosenCeleb),Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }

    public void newQuestion(){
        Random rand = new Random();

        chosenCeleb = rand.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();

        Bitmap celebImage = null;

        try {
            celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        celebImageView.setImageBitmap(celebImage);

        correct = rand.nextInt(4);

        int wrong;
        answers.clear();
        for(int i=0; i<4; i++){
            if(i == correct){
                answers.add(celebNames.get(chosenCeleb));
            } else {
                wrong = rand.nextInt(celebURLs.size());
                if(wrong == correct){
                    wrong = rand.nextInt(celebURLs.size());
                }
                answers.add(celebNames.get(wrong));
            }
        }
        button1.setText(answers.get(0));
        button2.setText(answers.get(1));
        button3.setText(answers.get(2));
        button4.setText(answers.get(3));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        celebImageView = (ImageView) findViewById(R.id.celebImageView);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();

        String result = null;

        try {
            result = task.execute("https://www.imdb.com/list/ls052283250").get();

            Pattern p = Pattern.compile("img alt=\"(.*?)\"(.*?)src=\"(.*?)\"",Pattern.DOTALL);
            Matcher m = p.matcher(result);

            while (m.find()){
                celebNames.add(m.group(1));
                //System.out.println(m.group(1));
                celebURLs.add(m.group(3));
                //System.out.println(m.group(3));
            }
            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}