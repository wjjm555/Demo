package com.jmc.recognitionapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "CJM";

    private final String API = "sCWw5a5WC2X786LARfKH6LwH", SECRET = "Nn1qus76TrMhZ5oKIuolwxPdU4tH0Epd";

    private File textDirectory, imageDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {

                Log.w(TAG, "onSuccess");
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.e(TAG, "onError:" + ocrError.getMessage());
            }
        }, getApplicationContext(), API, SECRET);

        initDirectory();

    }

    public void onClick(View view) {
//        final int max = 16;
//        for (int i = 1; i <= max; ++i) {
//            numbers.add(String.valueOf(i));
//        }

//        final String[] errIndexs = new String[]{ "12"};
//        for (int i = 0; i < errIndexs.length; ++i) {
//            numbers.add(errIndexs[i]);
//        }

        new Thread() {
            @Override
            public void run() {
                Log.e(TAG, "Thread Start");
                File[] folders = imageDirectory.listFiles();
                if (folders != null) {
                    recognizeFolder(new ArrayList<>(Arrays.asList(folders)));
                }


            }
        }.start();
    }

    private void recognizeFolder(final List<File> folders) {
        if (folders.size() > 0) {
            File folder = folders.remove(0);

            if (folder != null) {
                recognizeImages(folder, new RecognizeFolderListener() {

                    @Override
                    public void onRecognizeFolderFinish() {
                        recognizeFolder(folders);
                    }
                });
            }

        } else {
            Log.e(TAG, "Thread End");
        }


    }

    private void recognizeImages(File folder, RecognizeFolderListener listener) {
        if (folder != null) {
            File outFolder = new File(textDirectory, folder.getName());
            if (!outFolder.exists()) outFolder.mkdir();

            File[] files = folder.listFiles();

            if (files != null)
                recognizeFiles(outFolder, new ArrayList<>(Arrays.asList(files)), listener);

        }
    }


    private void recognizeFiles(final File outFolder, final List<File> files, final RecognizeFolderListener listener) {
        if (files.size() > 0) {
            final File file = files.remove(0);

            Log.i(TAG, "recognizeImages file:" + file);

            recognize(file, new RecognizeListener() {
                @Override
                public void onRecognizeBack(String file, String string) {
                    Log.w(TAG, "onRecognizeSuccess:" + file);
                    write(outFolder, file, string);
                    recognizeFiles(outFolder, files, listener);
                }

                @Override
                public void onRecognizeErr(String file) {
                    Log.e(TAG, "onRecognizeFail:" + file);
                    recognizeFiles(outFolder, files, listener);
                }
            });
        } else {
            if (listener != null) listener.onRecognizeFolderFinish();
        }
    }

    private void recognize(final File file, final RecognizeListener listener) {

        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(file);

        Log.i(TAG, "recognize step 2");//recognizeGeneralBasic recognizeAccurateBasic
        OCR.getInstance(this).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                StringBuilder stringBuilder = new StringBuilder();

                for (WordSimple wordSimple : generalResult.getWordList()) {
                    stringBuilder.append(wordSimple.getWords());
                }

                Log.i(TAG, "recognize onResult");
                listener.onRecognizeBack(file.getName(), stringBuilder.toString());
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.i(TAG, "onError:" + ocrError.getLocalizedMessage());
                listener.onRecognizeErr(file.getName());
            }
        });

    }

    private void write(File folder, String fileName, String string) {
        File file = new File(folder, fileName + ".txt");
        if (file.exists()) file.delete();

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            Log.i(TAG, "write step 2");
            fileWriter.write(string);
            fileWriter.flush();
            fileWriter.close();
            Log.i(TAG, "write step 3");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initDirectory() {
        File root = this.getExternalCacheDir();
        textDirectory = new File(root.getAbsoluteFile() + File.separator + "text");
        imageDirectory = new File(root.getAbsoluteFile() + File.separator + "image");

        if (!textDirectory.exists())
            textDirectory.mkdir();
        if (!imageDirectory.exists())
            imageDirectory.mkdir();

        Log.i(TAG, "textDirectory:" + textDirectory.getAbsolutePath());
    }

    interface RecognizeListener {
        void onRecognizeBack(String file, String string);

        void onRecognizeErr(String file);
    }

    interface RecognizeFolderListener {
        void onRecognizeFolderFinish();

    }
}
