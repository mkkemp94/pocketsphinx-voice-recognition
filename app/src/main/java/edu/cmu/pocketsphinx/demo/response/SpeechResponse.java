package edu.cmu.pocketsphinx.demo.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Response holder provided to the UI
 */
public class SpeechResponse
{
    public final SpeechStatus mSpeechStatus;
    
    @Nullable
    public final String mData;
    
    @Nullable
    public final String mError;
    
    /**
     * Note that this is private. Can only be accessed by inner methods.
     *
     * @param speechStatus the status of this response
     * @param data this response's data
     * @param error this response's error
     */
    private SpeechResponse(SpeechStatus speechStatus, @Nullable String data, @Nullable String error)
    {
        mSpeechStatus = speechStatus;
        mData = data;
        mError = error;
    }
    
    public static SpeechResponse yes(@NonNull String finalResponse)
    {
        return new SpeechResponse(SpeechStatus.YES, finalResponse, null);
    }
    
    public static SpeechResponse no()
    {
        return new SpeechResponse(SpeechStatus.NO, null, null);
    }
    
    public static SpeechResponse listening(String listening)
    {
        return new SpeechResponse(SpeechStatus.LISTENING, listening, null);
    }
    
    public static SpeechResponse working(@NonNull String partialResult)
    {
        return new SpeechResponse(SpeechStatus.WORKING, partialResult, null);
    
    }
    
    public static SpeechResponse timeout(@NonNull String timeout)
    {
        return new SpeechResponse(SpeechStatus.TIMEOUT, null, timeout);
    }
    
    public static SpeechResponse hypothesis(@NonNull String data)
    {
        return new SpeechResponse(SpeechStatus.HYPOTHESIS, data, null);
    }
    
    public static SpeechResponse error(@NonNull String error)
    {
        return new SpeechResponse(SpeechStatus.ERROR, null, error);
    }
}
