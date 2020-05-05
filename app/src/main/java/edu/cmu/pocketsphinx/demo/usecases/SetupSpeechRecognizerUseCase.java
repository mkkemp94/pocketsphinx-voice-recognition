package edu.cmu.pocketsphinx.demo.usecases;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import edu.cmu.pocketsphinx.demo.repository.SpeechRecognizerRepository;

import static edu.cmu.pocketsphinx.demo.VoiceRecognitionDemo.TAG;

public class SetupSpeechRecognizerUseCase
{
    private SpeechRecognizerRepository mSpeechRecognizerRepository;
    
    public SetupSpeechRecognizerUseCase(SpeechRecognizerRepository speechRecognizerRepository)
    {
        mSpeechRecognizerRepository = speechRecognizerRepository;
    }
    
    public void execute(MutableLiveData<String> mutableLiveData)
    {
        Log.d(TAG, "Setup from uc");
        mSpeechRecognizerRepository.setupRecognizer(mutableLiveData);
    }
}
