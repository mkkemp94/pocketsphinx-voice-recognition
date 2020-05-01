/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

/**
 * Source code originally obtained from:
 * https://github.com/cmusphinx/pocketsphinx
 */
public class VoiceRecognitionDemo extends Activity implements RecognitionListener
{
    private static final String TAG = "VoiceRecognitionDemo";
    
    /* Named searches allow to quickly reconfigure the decoder */
//    private static final String WAKEUP_SEARCH = "wakeup";
    private static final String PUNCH_ACTIONS = "punch_actions";
    
//    /* Keyword we are looking for to activate menu */
//    private static final String KEYPHRASE = "oh mighty computer";
    
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 19142;
    
    private SpeechRecognizer recognizer;
//    private HashMap<String, Integer> captions;
    
    private TextView mWorkingTextView;
    private TextView mCaptionTextView;
    private Button mButtonWakeup;
    
    private String mWorkingText = "";
    
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        
//        // Prepare the data for UI
//        captions = new HashMap<>();
////        captions.put(WAKEUP_SEARCH, R.string.wakeup_caption);
//        captions.put(MENU_SEARCH, R.string.instructions);
        setContentView(R.layout.main);
    
        mButtonWakeup = findViewById(R.id.btn_wakeup);
        mWorkingTextView = findViewById(R.id.result_text);
        mCaptionTextView = findViewById(R.id.caption_text);
        mCaptionTextView.setText("Preparing the recognizer.");
        mButtonWakeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mWorkingText = "";
                startVoiceRecognizer();
                setActivityToWakeup();
            }
        });
        
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if ( permissionCheck != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }
    
    private static class SetupTask extends AsyncTask<Void, Void, Exception>
    {
        WeakReference<VoiceRecognitionDemo> activityReference;
        
        SetupTask(VoiceRecognitionDemo activity)
        {
            this.activityReference = new WeakReference<>(activity);
        }
        
        @Override
        protected Exception doInBackground(Void... params)
        {
            try
            {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            }
            catch (IOException e)
            {
                return e;
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Exception result)
        {
            if ( result != null )
            {
                activityReference.get().showInstructions("Failed to init recognizer " + result);
            }
            else
            {
                activityReference.get().setActivityToIdle(); //switchSearch(WAKEUP_SEARCH);
            }
        }
    }
    
    private void setActivityToIdle()
    {
        Log.d(TAG, "setActivityToIdle");
//        stopVoiceRecognizer();
        showWakeupButton();
        hideInstructions();
        showWorkingText("");
    }
    
    private void setActivityToWakeup()
    {
        Log.d(TAG, "setActivityToWakeup");
        String instructions = getInstructionsString();
        showInstructions(instructions);
        hideWakeupButton();
    }
    
    private void showInstructions(String text)
    {
        Log.d(TAG, "showInstructions: " + text);
        mCaptionTextView.setText(text);
        mCaptionTextView.setVisibility(View.VISIBLE);
    }
    
    private void hideInstructions()
    {
        mCaptionTextView.setVisibility(View.GONE);
    }
    
    private void showWakeupButton()
    {
        mButtonWakeup.setVisibility(View.VISIBLE);
    }
    
    private void hideWakeupButton()
    {
        mButtonWakeup.setVisibility(View.GONE);
    }
    
    private String getInstructionsString()
    {
        return getResources().getString(R.string.instructions);
    }
    
    private void showWorkingText(String text)
    {
        mWorkingTextView.setText(text);
    }
    
    /**
     * Show final result here.
     */
    private void showFinalResult(String text)
    {
        makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if ( requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO )
        {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
            {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            }
            else
            {
                finish();
            }
        }
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
        if ( recognizer != null )
        {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }
    
    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        if ( hypothesis == null )
        {
            return;
        }
        
        mWorkingText = hypothesis.getHypstr();
        showWorkingText(mWorkingText);
    }
    
    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis)
    {
//        mResultTextView.setText("");
        if ( hypothesis != null )
        {
            mWorkingText = hypothesis.getHypstr();
//            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//            showWorkingText(text);
            showFinalResult(mWorkingText);
        }
    }
    
    @Override
    public void onBeginningOfSpeech()
    {
        showWorkingText("Listening...");
    }
    
    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech()
    {
//        if ( ! recognizer.getSearchName().equals(WAKEUP_SEARCH) )
//        {
//            switchSearch(WAKEUP_SEARCH);
//        }
        
        // TODO : Make a string builder and don't stop here if it's empty
        
        if ( ! mWorkingText.isEmpty() )
        {
            stopVoiceRecognizer();
            setActivityToIdle();
        }
//        showWorkingText("End of speech?");
//        setActivityToIdle();
    }
    
//    /**
//     * Stops the recognizer and starts listening to given search.
//     * @param searchName name of search to start listening for
//     */
//    private void switchSearch(String searchName)
//    {
//        stopVoiceRecognizer();
//
//        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
//        if ( searchName.equals(WAKEUP_SEARCH) )
//        {
//            startVoiceRecognizer(searchName);
//        }
//        else
//        {
//            recognizer.startListening(searchName, 10000);
//        }
//
//        String caption = getResources().getString(captions.get(searchName));
//        setCaption(caption);
//    }
    
    private void setupRecognizer(File assetsDir) throws IOException
    {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        recognizer = SpeechRecognizerSetup.defaultSetup()
                                          .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                                          .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
        
                                          .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
        
                                          .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */
        
        // Create keyword-activation search.
//        recognizer.addKeyphraseSearch(WAKEUP_SEARCH, KEYPHRASE);
        
        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(PUNCH_ACTIONS, menuGrammar);
    }
    
    @Override
    public void onError(Exception error)
    {
//        showWorkingText(error.getMessage());
        showFinalResult(error.getMessage());
        stopVoiceRecognizer();
        setActivityToIdle();
    }
    
    @Override
    public void onTimeout()
    {
//        showWorkingText("Timeout");
        showFinalResult("Timeout");
        stopVoiceRecognizer();
        setActivityToIdle();
//        switchSearch(WAKEUP_SEARCH);
    }
    
    private void stopVoiceRecognizer()
    {
        recognizer.stop();
    }
    
    private void startVoiceRecognizer()
    {
        recognizer.startListening(VoiceRecognitionDemo.PUNCH_ACTIONS, 10000);
    }
}
