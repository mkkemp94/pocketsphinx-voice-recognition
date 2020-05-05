package edu.cmu.pocketsphinx.demo.usecases;

import android.util.Log;

import edu.cmu.pocketsphinx.demo.data.SpeechRecognizerLocal;
import edu.cmu.pocketsphinx.demo.repository.SpeechRecognizerRepository;

import static edu.cmu.pocketsphinx.demo.VoiceRecognitionDemo.TAG;

public class StartSpeechRecognizerUseCase
{
    private SpeechRecognizerRepository mSpeechRecognizerRepository;
    
    public StartSpeechRecognizerUseCase(SpeechRecognizerRepository speechRecognizerRepository)
    {
        mSpeechRecognizerRepository = speechRecognizerRepository;
    }
    
    public void execute(SpeechRecognizerLocal.SpeechRecognizerCallback callback) // return MutableLiveData<SpeechResponse>
    {
        Log.d(TAG, "Start from uc");
        mSpeechRecognizerRepository.startSpeechRecognizer(callback);
    }
}
