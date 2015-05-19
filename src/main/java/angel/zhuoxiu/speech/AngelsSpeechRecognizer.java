package angel.zhuoxiu.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by zxui on 30/04/15.
 */
public class AngelsSpeechRecognizer implements RecognitionListener {
    static SoundPool soundPool;
    static int soundIdDang;

    public interface AngelsSpeechListener {
        public void onResult(String result);

        public void onPartialResult(String result);

        public void onError(String error);
    }

    String tag = this.getClass().getName();
    static Map<Locale, AngelsSpeechRecognizer> instanceMap = new HashMap<>();
    SpeechRecognizer speechRecognizer;
    Intent recognizerIntent;
    int dang;
    boolean isSpeeching = false;
    AngelsSpeechListener listener;
    Handler handler;
    public AngelsSpeechRecognizer(Context context, Locale locale) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);
        Log.d(tag, "AngelsSpeechRecognizer locale=" + locale.toString());
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toString())
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName())
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)

                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        handler=new Handler();
        if (soundPool == null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
            soundIdDang = soundPool.load(context, R.raw.dang, 1);
        }

    }

    public AngelsSpeechRecognizer setSpeechListener(AngelsSpeechListener listener) {
        this.listener = listener;
        return this;
    }

    public void startSpeech() {
        soundPool.play(soundIdDang, 1, 1, 1, 0, 1);
        isSpeeching = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speechRecognizer.startListening(recognizerIntent);
            }
        },200);
    }

    public void stopSpeech() {
        speechRecognizer.stopListening();
        isSpeeching = false;
    }

    public boolean isSpeeching() {
        return isSpeeching;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(tag, "AngelsSpeech onReadyForSpeech");

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(tag, "AngelsSpeech onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(tag, "AngelsSpeech onRmsChanged rmsdB=" + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(tag, "AngelsSpeech onBufferReceived buffer=" + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(tag, "AngelsSpeech onEndOfSpeech");
        soundPool.play(soundIdDang, 1, 1, 1, 0, 0.5f);
    }

    @Override
    public void onResults(Bundle results) {
        isSpeeching = false;
        List<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = new String();
        for (String result : matches) {
            text += result + "\n";
        }
        if (matches.size() > 0 && listener != null) {
            listener.onResult(matches.get(0));
        }
        Log.d(tag, "AngelsSpeech onResults text=" + text);

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        List<String> matches = partialResults
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = new String();
        for (String result : matches) {
            text += result + "\n";
        }
        if (matches.size() > 0 && listener != null) {
            listener.onPartialResult(matches.get(0));
        }
        Log.d(tag, "AngelsSpeech onPartialResults text=" + text);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(tag, "AngelsSpeech onEvent eventType=" + eventType);
    }

    @Override
    public void onError(int error) {
        isSpeeching = false;
        Log.i(tag, "AngelsSpeech onError error=" + getErrorText(error));
        if (listener != null) {
            listener.onResult(getErrorText(error));
        }
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
