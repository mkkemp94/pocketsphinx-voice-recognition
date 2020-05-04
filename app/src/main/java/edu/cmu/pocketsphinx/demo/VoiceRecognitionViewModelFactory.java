package edu.cmu.pocketsphinx.demo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class VoiceRecognitionViewModelFactory implements ViewModelProvider.Factory
{
    private ListenForCommandUseCase mListenForVoiceCommandUseCase;
    
    public VoiceRecognitionViewModelFactory(ListenForCommandUseCase listenForVoiceCommandUseCase)
    {
        mListenForVoiceCommandUseCase = listenForVoiceCommandUseCase;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        if ( modelClass.isAssignableFrom(VoiceRecognitionViewModel.class) )
        {
            return (T) new VoiceRecognitionViewModel(mListenForVoiceCommandUseCase);
        }
    
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
