package edu.cmu.pocketsphinx.demo;

import androidx.lifecycle.MutableLiveData;

public class ListenForCommandUseCase
{
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String PUNCH_ACTIONS = "punch_actions";
    private static final String VOICE_COMMAND_YES = "yes";
    private static final String VOICE_COMMAND_NO = "no";
    private static final String VOICE_TIMEOUT = "timeout";
    private static final String VOICE_LISTENING = "Listening...";
    
    ListenForCommandUseCase()
    {
    
    }
    
    public MutableLiveData<String> execute()
    {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        
        return mutableLiveData;
    }
}
