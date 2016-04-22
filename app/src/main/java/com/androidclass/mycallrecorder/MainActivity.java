package com.androidclass.mycallrecorder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {
    MediaRecorder recorder;
    Button startButton, stopButton;
    long startTime, stopTime;
    File audiofile, myDataPath;     // Datapath is the directory where the audiofile is to be stored

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton  = (Button) findViewById(R.id.stopButton);
    }

    public void startRecording(View view) throws IOException {

        String dateInString = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format( new Date() ).toString();
        String fileName = dateInString + ".3gp";

        myDataPath = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Jainams Innings");
        if ( !myDataPath.exists() ) myDataPath.mkdir();    // create new directory if the previous one doesn't exists
        audiofile = new File(myDataPath + "/" + fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        recorder.start();

        startTime = System.currentTimeMillis();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }


    public void stopRecording(View view) {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        stopTime = System.currentTimeMillis();
        stopButton.setEnabled(false);
        startButton.setEnabled(true);

        addRecordingToMediaLibrary();
    }

    protected void addRecordingToMediaLibrary() {
        //creating content values of size 4
        ContentValues values = new ContentValues();
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "MyAudio" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.DURATION, (int)((stopTime - startTime)/1000) );
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());       // this line _|_ transfers data from temp audio data file

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + audiofile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {}

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
