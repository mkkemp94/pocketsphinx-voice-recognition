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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import edu.cmu.pocketsphinx.demo.repository.SpeechRecognizerRepository;
import edu.cmu.pocketsphinx.demo.response.SpeechResponse;
import edu.cmu.pocketsphinx.demo.usecases.SetupSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.ShutdownSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StartSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StopSpeechRecognizerUseCase;

import static android.widget.Toast.makeText;

/**
 * Source code originally obtained from:
 * https://github.com/cmusphinx/pocketsphinx
 */
public class VoiceRecognitionDemo extends AppCompatActivity
{
    public static final String TAG = "VoiceRecognitionDemo";
    
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 19142;
    
    private AlertDialog mVerificationDialog;
    
    private TextView mWorkingTextView;
    private TextView mCaptionTextView;
    private Button mButtonStartRecognition;
    
    private SpeechRecognizerViewModel mSpeechRecognizerViewModel;
    
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.main);
        
        mButtonStartRecognition = findViewById(R.id.btn_wakeup);
        mWorkingTextView = findViewById(R.id.result_text);
        mCaptionTextView = findViewById(R.id.caption_text);
        mCaptionTextView.setText("Preparing the recognizer.");
        mButtonStartRecognition.setOnClickListener(view ->
                                                   {
                                                       Log.d(TAG, "Start recognizer from main button press");
                                                       mSpeechRecognizerViewModel.startSpeechRecognizer();
                                                       setActivityToWakeup();
                                                   });
        
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if ( permissionCheck != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        
        SpeechRecognizerRepository speechRecognizerRepository = new SpeechRecognizerRepository();
        SetupSpeechRecognizerUseCase setupSpeechRecognizerUseCase = new SetupSpeechRecognizerUseCase(speechRecognizerRepository);
        StartSpeechRecognizerUseCase startSpeechRecognizerUseCase = new StartSpeechRecognizerUseCase(speechRecognizerRepository);
        StopSpeechRecognizerUseCase stopSpeechRecognizerUseCase = new StopSpeechRecognizerUseCase(speechRecognizerRepository);
        ShutdownSpeechRecognizerUseCase shutdownSpeechRecognizerUseCase = new ShutdownSpeechRecognizerUseCase(speechRecognizerRepository);
        SpeechRecognizerViewModelFactory speechRecognizerViewModelFactory = new SpeechRecognizerViewModelFactory(setupSpeechRecognizerUseCase,
                                                                                                                 startSpeechRecognizerUseCase,
                                                                                                                 stopSpeechRecognizerUseCase,
                                                                                                                 shutdownSpeechRecognizerUseCase);
        mSpeechRecognizerViewModel = new ViewModelProvider(this, speechRecognizerViewModelFactory).get(SpeechRecognizerViewModel.class);
        
        mSpeechRecognizerViewModel.onSetupResponse()
                                  .observe(this, this::handleSetupResult);
    
        mSpeechRecognizerViewModel.onSpeechResponse()
                                  .observe(this, this::showFinalResult);
        
        Log.d(TAG, "Setup up from main oncreate");
        mSpeechRecognizerViewModel.setupSpeechRecognizer();
    }
    
    private void handleSetupResult(String error)
    {
        Log.d(TAG, "handleSetupResult: " + error);
        if ( error.equals("success") )
        {
            setActivityToIdle();
        }
        else
        {
            showFailedToInitRecognizer(error);
        }
    }
    
    private void shutdownSpeechRecognizer()
    {
        Log.d(TAG, "shutdownSpeechRecognizer");
        mSpeechRecognizerViewModel.shutdownSpeechRecognizer();
    }
    
    private void showFailedToInitRecognizer(String result)
    {
        Log.d(TAG, "showFailedToInitRecognizer");
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
    private void showFinalResult(final SpeechResponse result)
    {
        Log.d(TAG, "showFinalResult : " + result.mSpeechStatus + ", " + result.mData + ", " + result.mError);
        switch (result.mSpeechStatus)
        {
            case YES:
                // TODO Move forward from here
                makeText(getApplicationContext(), result.mData, Toast.LENGTH_SHORT).show();
                hideConfirmationDialog();
                setActivityToIdle();
                mSpeechRecognizerViewModel.stopSpeechRecognizer();
                break;
            case NO:
                hideConfirmationDialog();
                mSpeechRecognizerViewModel.stopSpeechRecognizer();
                mSpeechRecognizerViewModel.startSpeechRecognizer();
                break;
            case TIMEOUT:
                makeText(getApplicationContext(), result.mError, Toast.LENGTH_SHORT).show();
                showWorkingText(result.mError);
                hideConfirmationDialog();
                setActivityToIdle();
                mSpeechRecognizerViewModel.stopSpeechRecognizer();
                break;
            case LISTENING:
            case WORKING:
                showWorkingText(result.mData);
                break;
            case ERROR:
                showWorkingText(result.mError);
                hideConfirmationDialog();
                mSpeechRecognizerViewModel.stopSpeechRecognizer();
                break;
            case HYPOTHESIS:
                Log.d(TAG, "In hypo");
                hideConfirmationDialog();
                showConfirmationDialog(result.mData);
                mSpeechRecognizerViewModel.stopSpeechRecognizer();
                mSpeechRecognizerViewModel.startSpeechRecognizer();
                break;
            
            default:
                Log.e(TAG, "What is this? " + result.mSpeechStatus);
                break;
        }
    }
    
    private void showConfirmationDialog(final String text)
    {
        Log.d(TAG, "showConfirmationDialog: " + text);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("Punch Type");
        builder.setMessage("Do you want to " + text + "? Say yes or no.");
        builder.setCancelable(false);
        
        mVerificationDialog = builder.create();
        mVerificationDialog.show();
    }
    
    private void hideConfirmationDialog()
    {
        Log.d(TAG, "hideConfirmationDialog");
        if ( mVerificationDialog != null )
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
                mSpeechRecognizerViewModel.setupSpeechRecognizer();
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
        shutdownSpeechRecognizer();
    }
}
