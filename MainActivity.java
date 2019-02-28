package com.example.ibrahim.read;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String file = "sih.txt";
    private TextView textView;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        new LongOperation().execute("");

        Context context = getApplicationContext();
        ComponentName name = new ComponentName(context, NewAppWidget.class);
        int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
        updateAppWidget(MainActivity.this,appWidgetManager,ids.length);


    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String line, certstr = "";

            try {
                BufferedReader reader = new BufferedReader(new FileReader("/sdcard/filee.txt"));

                while( ( line = reader.readLine() ) != null)
                {
                    certstr += line;
                }
                reader.close();
//                Toast.makeText(MainActivity.this,certstr,Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e2) {
                Toast.makeText(getApplicationContext(), "File not found in /mnt/sdcard/", Toast.LENGTH_LONG).show();
                e2.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return certstr;
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,  int appWidgetId) {

        DateFormat format = SimpleDateFormat.getTimeInstance(
                SimpleDateFormat.MEDIUM, Locale.getDefault());
        CharSequence text = format.format(new Date());


        Intent intent = new Intent(context, NewAppWidget.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, 0);


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.new_app_widget);

        remoteViews.setTextViewText(R.id.appwidget_text, text);

        // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

}
