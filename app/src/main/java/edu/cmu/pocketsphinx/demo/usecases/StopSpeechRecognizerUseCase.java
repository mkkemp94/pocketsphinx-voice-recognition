package edu.cmu.pocketsphinx.demo.usecases;

import edu.cmu.pocketsphinx.demo.repository.SpeechRecognizerRepository;

public class StopSpeechRecognizerUseCase
{
    private SpeechRecognizerRepository mSpeechRecognizerRepository;
    
    public StopSpeechRecognizerUseCase(SpeechRecognizerRepository speechRecognizerRepository)
    {
        mSpeechRecognizerRepository = speechRecognizerRepository;
    }
    
    public void execute()
    {
        mSpeechRecognizerRepository.stopSpeechRecognizer();
    }
}
