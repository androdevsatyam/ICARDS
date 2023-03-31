package com.androdevsatyam.icards;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.androdevsatyam.icards.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    int PERMISSION_REQUEST_CODE = 101;
    Bitmap scaledbmp;
    ArrayList<CardModel> list = new ArrayList<>();
    int i = 0;
    String file_name = "";
    ProgressDialog creating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        creating = new ProgressDialog(this);

        importRecords();

        binding.save.setOnClickListener(v -> {
            if (checkPermission()) {
                creating.setMessage("Generating ICards please wait...");
                startGenerating();
            } else {
                requestPermission();
            }
        });
        binding.next.setOnClickListener(v -> {
            if (i == list.size() - 1) {
                binding.next.setVisibility(View.INVISIBLE);
                binding.prev.setVisibility(View.VISIBLE);
            } else {
                i = i + 1;
                setData(i);
            }
        });
        binding.prev.setOnClickListener(v -> {
            if (i == 0) {
                binding.prev.setVisibility(View.INVISIBLE);
                binding.next.setVisibility(View.VISIBLE);
            } else {
                i = i - 1;
                setData(i);
            }
        });
    }

    private void startGenerating() {
        creating.show();
        if (i < list.size()) {
            setData(i);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    binding.mainframe.setDrawingCacheEnabled(true);
                    scaledbmp = binding.mainframe.getDrawingCache();
                    scaledbmp = scaledbmp.copy(Bitmap.Config.ARGB_8888, true);
                    binding.mainframe.setDrawingCacheEnabled(false);
//      scaledbmp = BitmapFactory.decodeResource(getResources(), R.drawable.card_frame);
                    generatePDF();
                }
            }, 1000);
        } else {
            creating.dismiss();
            makeToast("Cards complete..");
        }

    }

    public void makeToast(String msg) {
        Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        if (msg.equalsIgnoreCase("Cards complete..")) {
            toast.setText(msg);
            toast.show();
        } else {
            toast.setText(msg);
            toast.show();
        }
    }

    private void setData(int i) {
        file_name = "";
        String name = list.get(i).getName();
        file_name = list.get(i).getCode() + ".pdf";
        binding.name.setText(list.get(i).getName());
        binding.designation.setText(list.get(i).getDesignation());

        if (name.contains("Vinay K. Goel") || name.contains("P.K. Thiagarajan") || name.contains("Neeraj Kapoor") || name.contains("R.K. Patel"))
            binding.id.setText("  ");
        else
            binding.id.setText(list.get(i).getCode());
        createQR(list.get(i).code.toLowerCase().replaceAll(" ", ""));
    }

    private void createQR(String code) {
        QRGEncoder qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT, 120);
        qrgEncoder.setColorBlack(Color.WHITE);
        qrgEncoder.setColorWhite(Color.BLACK);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            binding.qr.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.v("TAG", e.toString());
        }
    }

    private void generatePDF() {
        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();

        Log.d("generatePDF", "Width=" + scaledbmp.getWidth() + "\nHeight=" + scaledbmp.getHeight());
//        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(scaledbmp.getWidth(), scaledbmp.getHeight(), 1).create();
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(scaledbmp.getWidth(), scaledbmp.getHeight(), 1).create();
//        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(scaledbmp.getWidth(), scaledbmp.getHeight(), 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.
        canvas.drawBitmap(scaledbmp, 0, 0, paint);
        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
//        Typeface nametypeface = Typeface.createFromAsset(getAssets(), "font/budmo_jiggler.ttf");
//        Typeface namebold = Typeface.create(nametypeface, Typeface.BOLD);
//        Paint namepaint = new Paint();
//        namepaint.setTypeface(namebold);
//        namepaint.setTextSize(15);
//        canvas.drawText(binding.designation.getText().toString().toUpperCase(), 94, 248, namepaint);

//        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        // below line is used for setting text size
//        // which we will be displaying in our PDF file.
//        title.setTextSize(15);

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
//        canvas.drawText("A portal for IT professionals.", 209, 100, title);
//        canvas.drawText("Geeks for Geeks", 209, 80, title);

        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
//        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//        title.setColor(ContextCompat.getColor(this, R.color.purple_200));
//        title.setTextSize(15);

        // below line is used for setting
        // our text to center of PDF.
//        title.setTextAlign(Paint.Align.CENTER);
//        canvas.drawText("This is sample document which we have created.", 396, 560, title);

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of
        // our PDF file and its path.

        File file = new File(commonDocumentDirPath("IVCCARD"), file_name);

        if (file.exists()) {
            if (file.delete())
                file = new File(commonDocumentDirPath("IVCCARD"), file_name);
        }

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(file));
            // below line is to print toast message
            // on completion of PDF generation.
            runOnUiThread(() -> makeToast("PDF file generated successfully."));
        } catch (IOException e) {
            runOnUiThread(() -> makeToast("Exception=" + e.getLocalizedMessage()));
            e.printStackTrace();
        }
        pdfDocument.close();
        runOnUiThread(() -> {
            i = i + 1;
            startGenerating();
        });

    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    public static File commonDocumentDirPath(String FolderName) {
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + FolderName);
        } else {
            dir = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success) {
                dir = null;
            }
        }
        return dir;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void importRecords() {
        creating.setMessage("Generating resources...");
        creating.show();
        list.clear();
        InputStreamReader is = null;
        try {
//            is = new InputStreamReader(getAssets().open("accompanying_delegates.csv"));
//            is = new InputStreamReader(getAssets().open("delegates_users.csv"));
//            is = new InputStreamReader(getAssets().open("guest.csv"));
//            is = new InputStreamReader(getAssets().open("media.csv"));
//            is = new InputStreamReader(getAssets().open("organiser.csv"));
//            is = new InputStreamReader(getAssets().open("speaker.csv"));
            is = new InputStreamReader(getAssets().open("vip.csv"));
//            is = new InputStreamReader(getAssets().open("sponsor.csv"));
            BufferedReader reader = new BufferedReader(is);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                Log.d("TAG", "generateListDelegates: " + line);
                list.add(new CardModel(data[2], data[0].toString().equalsIgnoreCase("blank") ? "  " : data[0], data[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "generateListDelegates: " + list.size());
        creating.dismiss();
    }
}