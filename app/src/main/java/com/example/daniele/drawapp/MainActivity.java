package com.example.daniele.drawapp;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Array;


public class MainActivity extends AppCompatActivity {

    private DrawView drawView;
    private TextView textView;

    String modelFile="model.tflite";
    Interpreter tflite;

    Bitmap bitmap;

    int DIM_X=28;
    int DIM_Y=28;
    float MAX_BMP=255;

    float[][][][] inp=new float [1][DIM_X][DIM_Y][1];
    float[][] out=new float [1][10];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawView = findViewById(R.id.drawView);
        textView = findViewById(R.id.textView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawView.init(metrics);

        try {
            tflite = new Interpreter(loadModelFile(this, modelFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onButtonTap(View v){
        drawView.clear();
    }

    public void onButtonTapTest(View v){
        bitmap=drawView.getmBitmap();



        bitmap=ShrinkBitmap(bitmap, DIM_X, DIM_Y);

        ImageView imageView=findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        GenerateVector(bitmap);


        tflite.run(inp,out);
        textView.setText(String.valueOf(GetMaxIndex(out)));

    }


    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    private Bitmap ShrinkBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0,0, width, height, matrix, false);
        return resizedBitmap;
    }

    private void GenerateVector(Bitmap bm){
         for (int i = 0; i < DIM_X; i++) {
                for (int j = 0; j < DIM_Y; j++) {
                    inp[0][i][j][0] = 1-convertToGreyScale(bm.getPixel(i, j));
                }
            }
    }


    public static int GetMaxIndex(float[][] inputArray){
        float maxValue = inputArray[0][0];
        int maxIndex = 0;
        for(int i=1;i < inputArray[0].length;i++){
            if(inputArray[0][i] > maxValue){
                maxValue = inputArray[0][i];
                maxIndex = i;
                Log.i(String.valueOf(i), String.valueOf(inputArray[0][i]));
            }
        }
     //   return maxValue;
        return maxIndex;
    }

    private float convertToGreyScale(int color) {
        return (((color >> 16) & 0xFF) + ((color >> 8) & 0xFF) + (color & 0xFF)) / 3.0f / 255.0f;
    }
}