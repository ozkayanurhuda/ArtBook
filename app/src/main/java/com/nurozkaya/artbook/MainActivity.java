package com.nurozkaya.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> nameArray ;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        nameArray = new ArrayList<String>();
        idArray = new ArrayList<Integer>();


        //listeye bağlayıp yeni veri geldi demek için array adapter
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // i poistion nereye tıklandığı
                Intent intent = new Intent(MainActivity.this,Main2Activity.class); // main den main2 ye
                intent.putExtra("artId",idArray.get(i)); // kullanıcı nereye tıkladıysa o index buraya verilecek
                intent.putExtra("info","old"); // listview içinden tıklanırsa old olanı açmaya çalışıyo

                startActivity(intent);

            }
        });

        getData(); // data çekilir
    }

    //verileri çekmek
    public void getData () {

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);// database yoksa oluşturur
            Cursor cursor = database.rawQuery("SELECT * FROM arts",null);// cursor imleci ile query yapılır
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");
            while(cursor.moveToNext()) {
                nameArray.add(cursor.getString(nameIx));
                idArray.add(cursor.getInt(idIx));

            }

            // yeni bir veri ekledim listende göster
            arrayAdapter.notifyDataSetChanged();
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //bu akvititede hangi menüyü göstericez

        //Inflater

        MenuInflater menuInflater = getMenuInflater();  //menumuzu burdaki aktiviteye bağlıyoruz.
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // kullanıcı herhangi bir itemı seçerse ne yapacağımızı

        if (item.getItemId()==R.id.add_art_item) {  // add art itema tıklandıysa napıcaz.

            Intent intent = new Intent(MainActivity.this,Main2Activity.class);
            intent.putExtra("info","new"); // old değil yeni bir sanat ekleyecek

            startActivity(intent);

        }


        return super.onOptionsItemSelected(item);
    }
}