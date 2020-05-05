package edu.cmu.pocketsphinx.demo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import edu.cmu.pocketsphinx.demo.usecases.SetupSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.ShutdownSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StartSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StopSpeechRecognizerUseCase;

public class SpeechRecognizerViewModelFactory implements ViewModelProvider.Factory
{
    private SetupSpeechRecognizerUseCase mListenForVoiceCommandUseCase;
    private StartSpeechRecognizerUseCase mStartSpeechRecognizerUseCase;
    private StopSpeechRecognizerUseCase mStopSpeechRecognizerUseCase;
    private ShutdownSpeechRecognizerUseCase mShutdownSpeechRecognizerUseCase;
    
    SpeechRecognizerViewModelFactory(SetupSpeechRecognizerUseCase listenForVoiceCommandUseCase,
                                            StartSpeechRecognizerUseCase startSpeechRecognizerUseCase,
                                            StopSpeechRecognizerUseCase stopSpeechRecognizerUseCase,
                                            ShutdownSpeechRecognizerUseCase shutdownSpeechRecognizerUseCase)
    {
        mListenForVoiceCommandUseCase = listenForVoiceCommandUseCase;
        mStartSpeechRecognizerUseCase = startSpeechRecognizerUseCase;
        mStopSpeechRecognizerUseCase = stopSpeechRecognizerUseCase;
        mShutdownSpeechRecognizerUseCase = shutdownSpeechRecognizerUseCase;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        if ( modelClass.isAssignableFrom(SpeechRecognizerViewModel.class) )
        {
            return (T) new SpeechRecognizerViewModel(mListenForVoiceCommandUseCase,
                                                     mStartSpeechRecognizerUseCase,
                                                     mStopSpeechRecognizerUseCase,
                                                     mShutdownSpeechRecognizerUseCase);
        }
    
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
