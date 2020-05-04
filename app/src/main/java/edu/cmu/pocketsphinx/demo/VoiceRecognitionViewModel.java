package edu.cmu.pocketsphinx.demo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VoiceRecognitionViewModel extends ViewModel
{
    private ListenForCommandUseCase mListenForCommandUseCase;
    
    VoiceRecognitionViewModel(ListenForCommandUseCase listenForVoiceCommandUseCase)
    {
    
        mListenForCommandUseCase = listenForVoiceCommandUseCase;
    }
    
    MutableLiveData<String> startListeningForVoiceCommands()
    {
        return mListenForCommandUseCase.execute();
    }
}
