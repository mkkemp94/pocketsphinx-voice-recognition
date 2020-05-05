package edu.cmu.pocketsphinx.demo.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import edu.cmu.pocketsphinx.demo.data.SpeechRecognizerLocal;

import static edu.cmu.pocketsphinx.demo.VoiceRecognitionDemo.TAG;

public class SpeechRecognizerRepository
{
    private final SpeechRecognizerLocal mSpeechRecognizerLocal;
    
    public SpeechRecognizerRepository()
    {
        mSpeechRecognizerLocal = new SpeechRecognizerLocal();
    }
    
    public void setupRecognizer(MutableLiveData<String> mutableLiveData)
    {
        Log.d(TAG, "Setting up from repo");
        mSpeechRecognizerLocal.setupRecognizer(mutableLiveData);
    }
    
    public void startSpeechRecognizer(SpeechRecognizerLocal.SpeechRecognizerCallback callback)
    {
        Log.d(TAG, "Start from repo");
        mSpeechRecognizerLocal.startSpeechRecognizer(callback);
    }
    
    public void shutdownSpeechRecognizer()
    {
        mSpeechRecognizerLocal.shutdownSpeechRecognizer();
    }
    
    public void stopSpeechRecognizer()
    {
        mSpeechRecognizerLocal.stopSpeechRecognizer();
    }
}
