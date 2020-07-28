package com.nurozkaya.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    Bitmap selectedImage; // başta tanımlamam gerekenler
    ImageView imageView;
    EditText artNameText, painterNameText,yearText;
    Button button;
    SQLiteDatabase database ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView); // onCreate de tanımlamam gereknler
        artNameText = findViewById(R.id.artNameText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText = findViewById(R.id.yearText);
        button = findViewById(R.id.button);

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        // yollanan intenti alıyoruz

        Intent intent  = getIntent();
        String info = intent.getStringExtra("info"); // infoda new veya old olucak
        if (info.matches("new")) { // new olduğundan emin olalım edit textleri boş hale getirelim

            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");

            button.setVisibility(View.VISIBLE); // new de save butonu olsun oldda olmasın

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            imageView.setImageBitmap(selectImage);

        } else {  // eskiden geliyorsa

            int artId = intent.getIntExtra("artId",1); // eger bir yanlışlık olursa ilk resmi gösteririz

            button.setVisibility(View.INVISIBLE); //eski şeyi gösteriyorsa save butonu gizle

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[]{String.valueOf(artId)}); // selection argümanı

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    artNameText.setText(cursor.getString(artNameIx));
                    painterNameText.setText(cursor.getString(painterNameIx));
                    yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx); // görseli byte dizisi olarak kaydettik, bitmape çeviricez
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length); //byte dizisini görsel hale getiren kod
                    imageView.setImageBitmap(bitmap);
                }

            } catch (Exception e) {

            }
        }
    }


    public void selectImage (View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {  // galeriye götürücez intent ile
            Intent intentToGallery = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // galeriden foto alma
            startActivityForResult(intentToGallery,2);
        }
    }

    @Override    // izin istendiğinde ne olacağı
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // grant result verilen değerler

        if ( requestCode == 1) {
            if(grantResults.length> 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                Intent intentToGallery = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // kullanıcıyı alıp galeriye götürüyorum
                startActivityForResult(intentToGallery,2); // bir sonuç için aktivite başlatıyoruz.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==2 && resultCode==RESULT_OK && data != null) {
            Uri imageData = data.getData();
            try { // kendisi try and catch içine soktu

                if (Build.VERSION.SDK_INT>=28) {// telin inti 28 den büyük ve eşitse (yeni metod) yerine imagedecoder geldi
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                }
                // eskisi için
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData); // üstü çiziliyi kullandım eski telefonlar için
                imageView.setImageBitmap(selectedImage);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save (View view) {

        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString(); // neden string kaydettik??

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        //görsel kaydetme

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // outputstream için bunu yapmalıyım
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream); // selectedımage ı small image a çevirdim
        byte[] byteArray = outputStream.toByteArray(); // istediğim görseli veriye çevirme işini tamamlıyoruz

        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null); // database oluşturulur.
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)"); // table oluşturulur. verileri blob olarak kaydediyoruz

            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)"; // değerleri bilmiyorum.?
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString); // bir stringi sql de sql komutu gibi çalıştırmya yarar
            sqLiteStatement.bindString(1, artName); // soru işaretleri değişkenle bağlanır bind
            sqLiteStatement.bindString(2, painterName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray); // image yerine byte array
            sqLiteStatement.execute();


        } catch (Exception e) {

        }

        Intent intent = new Intent(Main2Activity.this,MainActivity.class);

        // intent yaparken bütün aktiviteleri kapatıcam diyorum
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // geri basınca uygulama kapanıyor
        startActivity(intent); // bi önceki sayfaya geri dönme sorunu yapıyor

        //finish(); intent yapınnca onCreate çağırılacak

    }

    public Bitmap makeSmallerImage (Bitmap image, int maximumSize) { // görsel küçültmek , aynı oranda küçültüyor
        int width = image.getWidth(); // uzunluk ve genişlik alıyorum
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height; // her ikisini de floata çeviriyorum ve birbirine bölüp oran buluyorum
        if(bitmapRatio > 1){ // resim yatay demek, eni maximum olmalı
            width = maximumSize;
            height = (int) (width / bitmapRatio);

        } else { // resim dikey demek, boyu maximum olmalı
            height = maximumSize;
            width = (int) (height * bitmapRatio); // birden küçükse çarpı yapıyoruz , bölmek daha da büyütür.
        }
        return Bitmap.createScaledBitmap(image,width,height,true); // küçük bir bitmap oluşturdu
    }
}