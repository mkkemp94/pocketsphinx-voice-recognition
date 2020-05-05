package edu.cmu.pocketsphinx.demo.data;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import androidx.lifecycle.MutableLiveData;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import edu.cmu.pocketsphinx.demo.AtsApplication;

import static edu.cmu.pocketsphinx.demo.VoiceRecognitionDemo.TAG;

public class SpeechRecognizerLocal implements RecognitionListener
{
    private static final String PUNCH_ACTIONS = "punch_actions";
    
    public interface SpeechRecognizerCallback
    {
        void onListening();
    
        void onTimeout();
    
        void onPartialResult(String partialResult);
        
        void onEndOfSpeech(String result);
        
        void onError(String error);
    }
    
    private SpeechRecognizerCallback mSpeechRecognizerCallback;
    private SpeechRecognizer mSpeechRecognizer;
    
    private String mWorkingText = "";
    
    public void setupRecognizer(final MutableLiveData<String> mutableLiveData)
    {
        Log.d(TAG, "setupRecognizer local");
    
        new Thread(() ->
                   {
                       try
                       {
                           Assets assets = new Assets(AtsApplication.getApplication());
                           File assetDir = assets.syncAssets();
                           setupRecognizer(assetDir);
    
                           Log.d(TAG, "Setup success local");
    
                           mutableLiveData.postValue("success");
                       }
                       catch (IOException e)
                       {
                           Log.d(TAG, "Setup fail local");
    
                           mutableLiveData.postValue(e.getMessage());
                       }
                   }
        ).start();
    }
    
    /**
     * Called off UI thread
     *
     * @param assetsDir
     * @throws IOException
     */
    private void setupRecognizer(File assetsDir) throws IOException
    {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        mSpeechRecognizer = SpeechRecognizerSetup.defaultSetup()
                                                 .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                                                 .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
        
                                                 .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
        
                                                 .getRecognizer();
        mSpeechRecognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */
        
        // Create keyword-activation search.
        //        recognizer.addKeyphraseSearch(WAKEUP_SEARCH, KEYPHRASE);
        
        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        mSpeechRecognizer.addGrammarSearch(PUNCH_ACTIONS, menuGrammar);
        
        //        File digitGrammar = new File(assetsDir, "digits.gram");
        //        recognizer.addGrammarSearch(CONFIRMATION_ACTIONS, digitGrammar);
    }
    
    public void startSpeechRecognizer(SpeechRecognizerCallback speechRecognizerCallback)
    {
        Log.d(TAG, "startSpeechRecognizer local");
        
        mWorkingText = "";
        
        if (mSpeechRecognizerCallback == null)
        {
            mSpeechRecognizerCallback = speechRecognizerCallback;
        }
        
        mSpeechRecognizer.startListening(PUNCH_ACTIONS, 10000);
    }
    
    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        if ( hypothesis == null )
        {
            return;
        }
        
        mWorkingText = hypothesis.getHypstr();
        
        Log.d(TAG, "Partial Result local: " + mWorkingText);
        if (mSpeechRecognizerCallback != null)
            mSpeechRecognizerCallback.onPartialResult(mWorkingText);
    }
    
    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis)
    {
        if ( hypothesis != null )
        {
            mWorkingText = hypothesis.getHypstr();
            Log.d(TAG, "onResult: " + mWorkingText);
        }
    }
    
    @Override
    public void onBeginningOfSpeech()
    {
        Log.d(TAG, "onBeginningOfSpeech");
        if (mSpeechRecognizerCallback != null)
            mSpeechRecognizerCallback.onListening();
    }
    
    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech()
    {
        if ( ! mWorkingText.isEmpty() )
        {
            Log.d(TAG, "onEndOfSpeech: " + mWorkingText);
            if (mSpeechRecognizerCallback != null)
                mSpeechRecognizerCallback.onEndOfSpeech(mWorkingText);
        }
    }
    
    @Override
    public void onError(Exception error)
    {
        Log.e(TAG, "onError: " + error.getMessage());
        if (mSpeechRecognizerCallback != null)
            mSpeechRecognizerCallback.onError(error.getMessage());
    }
    
    @Override
    public void onTimeout()
    {
        Log.e(TAG, "onTimeout local");
        if (mSpeechRecognizerCallback != null)
            mSpeechRecognizerCallback.onTimeout();
    }
    
    public void shutdownSpeechRecognizer()
    {
        if ( mSpeechRecognizer != null )
        {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.shutdown();
        }
    }
    
    public void stopSpeechRecognizer()
    {
        mSpeechRecognizerCallback = null;
        mSpeechRecognizer.stop();
    }
}
