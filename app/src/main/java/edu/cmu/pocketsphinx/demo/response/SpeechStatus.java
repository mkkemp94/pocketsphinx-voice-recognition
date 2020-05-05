package edu.cmu.pocketsphinx.demo.response;

/**
 * Possible status types of a response provided to the UI
 * <p>
 * LOADING, SUCCESS, or ERROR
 */
public enum SpeechStatus
{
    YES,
    NO,
    TIMEOUT,
    WORKING,
    LISTENING,
    HYPOTHESIS,
    ERROR
}
