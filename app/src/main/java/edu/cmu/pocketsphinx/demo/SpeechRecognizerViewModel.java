package edu.cmu.pocketsphinx.demo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import edu.cmu.pocketsphinx.demo.data.SpeechRecognizerLocal;
import edu.cmu.pocketsphinx.demo.response.SpeechResponse;
import edu.cmu.pocketsphinx.demo.usecases.SetupSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.ShutdownSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StartSpeechRecognizerUseCase;
import edu.cmu.pocketsphinx.demo.usecases.StopSpeechRecognizerUseCase;

import static edu.cmu.pocketsphinx.demo.VoiceRecognitionDemo.TAG;

class SpeechRecognizerViewModel extends ViewModel
{
    private static final String VOICE_COMMAND_YES = "yes";
    private static final String VOICE_COMMAND_NO = "no";
    
    private static final String VOICE_TIMEOUT = "Timeout";
    private static final String VOICE_LISTENING = "Listening...";
    
    private static final String[] PUNCH_ARRAY = new String[] {
            "punch in",
            "punch out",
            "meal in",
            "meal out",
            "break in",
            "break out",
            "transfer"
    };
    
    // Count on this stopping itself
    private SetupSpeechRecognizerUseCase mSetupSpeechRecognizerUseCase;
    private StartSpeechRecognizerUseCase mStartSpeechRecognizerUseCase;
    private StopSpeechRecognizerUseCase mStopSpeechRecognizerUseCase;
    private ShutdownSpeechRecognizerUseCase mShutdownSpeechRecognizerUseCase;
    
    private MutableLiveData<String> mSetupResponse = new MutableLiveData<>();
    private MutableLiveData<SpeechResponse> mSpeechResponse = new MutableLiveData<>();
    
    private String mFinalResult = "";
    
    SpeechRecognizerViewModel(SetupSpeechRecognizerUseCase setupSpeechRecognizerUseCase,
                              StartSpeechRecognizerUseCase startSpeechRecognizerUseCase,
                              StopSpeechRecognizerUseCase stopSpeechRecognizerUseCase,
                              ShutdownSpeechRecognizerUseCase shutdownSpeechRecognizerUseCase)
    {
        mSetupSpeechRecognizerUseCase = setupSpeechRecognizerUseCase;
        mStartSpeechRecognizerUseCase = startSpeechRecognizerUseCase;
        mStopSpeechRecognizerUseCase = stopSpeechRecognizerUseCase;
        mShutdownSpeechRecognizerUseCase = shutdownSpeechRecognizerUseCase;
    }
    
    LiveData<String> onSetupResponse()
    {
        return mSetupResponse;
    }
    
    LiveData<SpeechResponse> onSpeechResponse()
    {
        return mSpeechResponse;
    }
    
    void setupSpeechRecognizer()
    {
        Log.d(TAG, "Setup from vm");
        mSetupSpeechRecognizerUseCase.execute(mSetupResponse);
    }
    
    void startSpeechRecognizer()
    {
        Log.d(TAG, "Start from vm");
        mStartSpeechRecognizerUseCase.execute(mSpeechRecognizerCallback);
    }
    
    private SpeechRecognizerLocal.SpeechRecognizerCallback mSpeechRecognizerCallback = new SpeechRecognizerLocal.SpeechRecognizerCallback()
    {
        @Override
        public void onListening()
        {
            Log.d(TAG, "Listening...");
            mSpeechResponse.setValue(SpeechResponse.listening(VOICE_LISTENING));
        }
    
        @Override
        public void onTimeout()
        {
            Log.d(TAG, "Timeout");
            mSpeechResponse.setValue(SpeechResponse.timeout(VOICE_TIMEOUT));
        }
    
        @Override
        public void onEndOfSpeech(String result)
        {
            Log.d(TAG, "End of speech: " + result);
        
            if (result.equals(VOICE_COMMAND_YES))
            {
                mSpeechResponse.setValue(SpeechResponse.yes(mFinalResult));
            }
            else if (result.equals(VOICE_COMMAND_NO))
            {
                mFinalResult = "";
                mSpeechResponse.setValue(SpeechResponse.no());
            }
            else if (punchArrayContains(result))
            {
                // hypothesis
                mFinalResult = result;
                mSpeechResponse.setValue(SpeechResponse.hypothesis(result));
            }
            else
            {
                mSpeechResponse.setValue(SpeechResponse.working(result));
            }
        }
    
        @Override
        public void onError(String error)
        {
            mSpeechResponse.setValue(SpeechResponse.error(error));
        }
    };
    
    private boolean punchArrayContains(String result)
    {
        for (String element : PUNCH_ARRAY)
        {
            if (element.equals(result))
                return true;
        }
        
        return false;
    }
    
    void stopSpeechRecognizer()
    {
        Log.d(TAG, "Stop from vm");
        mStopSpeechRecognizerUseCase.execute();
    }
    
    void shutdownSpeechRecognizer()
    {
        mShutdownSpeechRecognizerUseCase.execute();
    }
}
