package edu.cmu.pocketsphinx.demo.usecases;

import edu.cmu.pocketsphinx.demo.repository.SpeechRecognizerRepository;

public class ShutdownSpeechRecognizerUseCase
{
    private SpeechRecognizerRepository mSpeechRecognizerRepository;
    
    public ShutdownSpeechRecognizerUseCase(SpeechRecognizerRepository speechRecognizerRepository)
    {
        mSpeechRecognizerRepository = speechRecognizerRepository;
    }
    
    public void execute()
    {
        mSpeechRecognizerRepository.shutdownSpeechRecognizer();
    }
}
