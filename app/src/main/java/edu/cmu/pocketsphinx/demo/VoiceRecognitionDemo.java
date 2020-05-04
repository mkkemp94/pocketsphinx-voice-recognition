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
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
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
public class VoiceRecognitionDemo extends AppCompatActivity implements RecognitionListener
{
    private static final String TAG = "VoiceRecognitionDemo";
    
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String PUNCH_ACTIONS = "punch_actions";
    private static final String VOICE_COMMAND_YES = "yes";
    private static final String VOICE_COMMAND_NO = "no";
    private static final String VOICE_TIMEOUT = "timeout";
    private static final String VOICE_LISTENING = "Listening...";
    
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 19142;
    
    private SpeechRecognizer recognizer;
    
    private AlertDialog mVerificationDialog;
    
    private TextView mWorkingTextView;
    private TextView mCaptionTextView;
    private Button mButtonStartRecognition;
    
    private String mResult = "";
    private String mWorkingText = "";
    
    private VoiceRecognitionViewModel mVoiceRecognitionViewModel;
    
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.main);
    
        mButtonStartRecognition = findViewById(R.id.btn_wakeup);
        mWorkingTextView = findViewById(R.id.result_text);
        mCaptionTextView = findViewById(R.id.caption_text);
        mCaptionTextView.setText("Preparing the recognizer.");
        mButtonStartRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mWorkingText = "";
                mResult = "";
                startVoiceReceiver();
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
    
        ListenForCommandUseCase listenForCommandUseCase = new ListenForCommandUseCase();
        VoiceRecognitionViewModelFactory voiceRecognitionViewModelFactory = new VoiceRecognitionViewModelFactory(listenForCommandUseCase);
        mVoiceRecognitionViewModel = new ViewModelProvider(this, voiceRecognitionViewModelFactory)
                .get(VoiceRecognitionViewModel.class);
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
                activityReference.get().showFailedToInitRecognizer(result);
            }
            else
            {
                activityReference.get().setActivityToIdle();
            }
        }
    }
    
    private void showFailedToInitRecognizer(Exception result)
    {
        String error = "Failed to init recognizer " + result;
        mCaptionTextView.setText(error);
        mCaptionTextView.setVisibility(View.VISIBLE);
    }
    
    private void setActivityToIdle()
    {
        Log.d(TAG, "setActivityToIdle");
        showWakeupButton();
        hideInstructions();
        showWorkingText("");
    }
    
    private void setActivityToWakeup()
    {
        Log.d(TAG, "setActivityToWakeup");
        hideWakeupButton();
        showInstructions();
    }
    
    private void showInstructions()
    {
        String text = getInstructionsString();
        mCaptionTextView.setText(text);
        mCaptionTextView.setVisibility(View.VISIBLE);
        Log.d(TAG, "showInstructions: " + text);
    }
    
    private void hideInstructions()
    {
        mCaptionTextView.setVisibility(View.GONE);
    }
    
    private void showWakeupButton()
    {
        mButtonStartRecognition.setVisibility(View.VISIBLE);
    }
    
    private void hideWakeupButton()
    {
        mButtonStartRecognition.setVisibility(View.GONE);
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
    private void showFinalResult(final String result)
    {
        hideConfirmationDialog();
        switch (result)
        {
            case VOICE_COMMAND_YES:
                // TODO Move forward from here
                makeText(getApplicationContext(), mResult, Toast.LENGTH_SHORT).show();
                hideConfirmationDialog();
                setActivityToIdle();
                stopVoiceRecognizer();
                break;
            case VOICE_COMMAND_NO:
                hideConfirmationDialog();
                restartVoiceRecognizer();
                break;
            case VOICE_TIMEOUT:
                makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                hideConfirmationDialog();
                setActivityToIdle();
                stopVoiceRecognizer();
                break;
            default:
                mResult = result;
                showConfirmationDialog(result);
                restartVoiceRecognizer();
                break;
        }
    }
    
    private void showConfirmationDialog(final String text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("Punch Type");
        builder.setMessage("Do you want to " + text + "? Say yes or no.");
        builder.setCancelable(false);
    
        mVerificationDialog = builder.create();
        mVerificationDialog.show();
    }
    
    private void hideConfirmationDialog()
    {
        if ( mVerificationDialog != null)
        {
            mVerificationDialog.dismiss();
        }
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
        if ( hypothesis != null )
        {
            mWorkingText = hypothesis.getHypstr();
        }
    }
    
    @Override
    public void onBeginningOfSpeech()
    {
        showWorkingText(VOICE_LISTENING);
    }
    
    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech()
    {
        if ( ! mWorkingText.isEmpty() )
        {
            showFinalResult(mWorkingText);
        }
    }
    
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
    
//        File digitGrammar = new File(assetsDir, "digits.gram");
//        recognizer.addGrammarSearch(CONFIRMATION_ACTIONS, digitGrammar);
    }
    
    @Override
    public void onError(Exception error)
    {
        showFinalResult(error.getMessage());
    }
    
    @Override
    public void onTimeout()
    {
        showFinalResult(VOICE_TIMEOUT);
    }
    
    private void stopVoiceRecognizer()
    {
        recognizer.stop();
    }
    
    private void startVoiceReceiver()
    {
        recognizer.startListening(VoiceRecognitionDemo.PUNCH_ACTIONS, 10000);
    }
    
    private void restartVoiceRecognizer()
    {
        stopVoiceRecognizer();
        startVoiceReceiver();
    }
}
