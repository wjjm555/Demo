package com.jmc.recognitionapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.EnvironmentCompat;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        final String name = "国际商务", file = "4";
        final List<String> numbers = new ArrayList<>();

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
                recognizeImages(file, name, numbers);

            }
        }.start();
    }

    private void recognizeImages(final String fileDir, final String outFile, final List<String> fileIndexArr) {
        if (fileIndexArr.size() > 0) {
            final String fileIndex = fileIndexArr.remove(0);

            Log.i(TAG, "recognizeImages fileIndex:" + fileIndex);

            recognize(fileDir, fileIndex, new RecognizeListener() {
                @Override
                public void onRecognizeBack(String file, String string) {
                    Log.w(TAG, "onRecognizeSuccess:" + file);
                    write(outFile, fileIndex, string);
                    recognizeImages(fileDir, outFile, fileIndexArr);
                }

                @Override
                public void onRecognizeErr(String file) {
                    Log.e(TAG, "onRecognizeFail:" + file);
                    recognizeImages(fileDir, outFile, fileIndexArr);
                }
            });
        } else {
            Log.e(TAG, "Thread End");
        }

    }

    private void recognize(String fileDir, final String fileName, final RecognizeListener listener) {
        final String filePath = imageDirectory.getAbsolutePath() + File.separator + fileDir + File.separator + fileName + ".png";

        Log.i(TAG, "recognize step 1 :filePath:" + filePath);

        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));

        Log.i(TAG, "recognize step 2");//recognizeGeneralBasic recognizeAccurateBasic
        OCR.getInstance(this).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                StringBuilder stringBuilder = new StringBuilder();

                for (WordSimple wordSimple : generalResult.getWordList()) {
                    stringBuilder.append(wordSimple.getWords());
                }

                Log.i(TAG, "recognize onResult");
                listener.onRecognizeBack(fileName, stringBuilder.toString());
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.i(TAG, "onError:" + ocrError.getLocalizedMessage());
                listener.onRecognizeErr(fileName);
            }
        });

    }

    private void write(String fileName, String fileIndex, String string) {
        String filePath = textDirectory.getAbsolutePath() + File.separator + fileName + fileIndex + ".txt";
        Log.i(TAG, "write step 1 filePath：" + filePath);

        File file = new File(filePath);
        if (file.exists()) file.delete();

        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
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
}
