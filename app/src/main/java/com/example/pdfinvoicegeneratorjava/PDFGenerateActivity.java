package com.example.pdfinvoicegeneratorjava;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerateActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final String TAG = "XOKSIS";
    Button btnCreate;
    EditText editText;
    int pdfHeight = 1080;
    int pdfWidth = 720;
    private PdfDocument document;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdfgenerate);

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
*/


            btnCreate = (Button) findViewById(R.id.createFromText);
            editText = (EditText) findViewById(R.id.editText);

            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkPermission()) {
                        generatePDF();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            requestPermission();
                        }
                    }
                }
            });
    }

    private void generatePDF() {
        document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pdfWidth, pdfHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paintText = new Paint();
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL));
        paintText.setTextSize(25);
        paintText.setColor(ContextCompat.getColor(this, R.color.black));
        paintText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("(PDF by XAIHOF)", 369, 50, paintText);

        paintText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        paintText.setColor(ContextCompat.getColor(this, R.color.red));
        paintText.setTextSize(17);
        paintText.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(((EditText) findViewById(R.id.editText)).getText().toString(), 50, 100, paintText);
        document.finishPage(page);
        createFile();
    }

    private static final int CREATE_FILE = 1;

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");
        startActivityForResult(intent, CREATE_FILE);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 23);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // ANDROID IS 11(R) OR ABOVE.
            return Environment.isExternalStorageManager();
        } else {
            int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();

                if (document != null) {
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd = getContentResolver().openFileDescriptor(uri, "w");
                        FileOutputStream fileOutputStream = new FileOutputStream((pfd.getFileDescriptor()));
                        document.writeTo(fileOutputStream);
                        document.close();
                        Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        try {
                            DocumentsContract.deleteDocument(getContentResolver(), uri);
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            } else {

            }
        }
    }

    public void createPdfFromView(View view) {
        final Dialog invoicedialog = new Dialog(this);
        invoicedialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        invoicedialog.setContentView(R.layout.invoice_layout);
        invoicedialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(invoicedialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        invoicedialog.getWindow().setAttributes(lp);
        Button downloadinvoicebtn = invoicedialog.findViewById(R.id.downloadinvoicebtn);
        invoicedialog.show();

        downloadinvoicebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePdfFromView(invoicedialog.findViewById(R.id.cardview));
            }
        });

    }

    private void generatePdfFromView(View view) {
        Bitmap bitmap = getBitmapFromView(view);
        document = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page myPage = document.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(myPage);
        createFile();
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    public void KotlinActivity(View view) {
    }
}