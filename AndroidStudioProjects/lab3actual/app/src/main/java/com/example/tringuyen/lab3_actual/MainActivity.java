package com.example.tringuyen.lab3_actual;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.app.*;
import android.content.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> arr;
    AlertDialog actions;
    int currentPos=0;
    EditText txt;
    int counter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (EditText) findViewById(R.id.editTxt); //text from editText to add more stuffs in

        final List<String> todoList = new ArrayList<String>(); // arraylist keeping track of preset element
        todoList.add("groceries"); todoList.add("wash car"); todoList.add("pick up drycleaning"); todoList.add("library"); todoList.add("clean basement");

        ListView listView = (ListView) findViewById(R.id.list_item); // listView
        arr = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        listView.setAdapter(arr);//set listview adapter


        for(int i=0; i<todoList.size();i++){
            arr.add(todoList.get(i));
        }



        //onItemClick will set add item with "Done:" and move it to the bottom, if item have "Done:", then remove "Done:" and move it top of the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "Removing " + ((TextView) view).getText(), Toast.LENGTH_SHORT).show();

                String a = arr.getItem(position);
                if(a.contains("Done:")){
                    String i = a.replaceAll("Done:", "");
                    arr.remove(arr.getItem(position));
                    arr.insert(i, 0);
                }else {
                    String i = "Done:" + arr.getItem(position);
                    arr.remove(arr.getItem(position));
                    arr.add(i);
                }

                //arr.add(i);
                //arr.remove(arr.getItem(position));
            }
        });

        //onlongclick will pop up the delete message, if accepted, item will be deleted
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                                    currentPos = position;
                                                    actions.show();
                                                    return true;
                                                }
                                            }
        );
        //dialog for popup
        DialogInterface.OnClickListener actionListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Delete
                                arr.remove(arr.getItem(currentPos));
                                break;
                            default:
                                break;
                        }
                    }
                };

        AlertDialog.Builder builder = new
                AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete this item?");
        String[] options = {"Delete"};
        builder.setItems(options, actionListener);
        builder.setNegativeButton("Cancel", null);
        actions = builder.create();
    }

    //delete "Done:" item button
    public void deleteBtn(View v){
        ArrayList<Integer> intTemp = new ArrayList<Integer>();
        for(int i=0; i<arr.getCount(); i++){
            Log.d(arr.getItem(i), "deleteBtn: ");
            if( arr.getItem(i).contains("Done:")) {
                arr.remove(arr.getItem(i));
            }
        }
    }

    //add item button
    public void addBtnOnClick(View v){
        String temp = txt.getText().toString();
        arr.add(temp);
        txt.setText("");
    }

}
