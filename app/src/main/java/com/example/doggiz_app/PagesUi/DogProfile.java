package com.example.doggiz_app.PagesUi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doggiz_app.Backend.MainActivity;
import com.example.doggiz_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DogProfile extends Fragment {

    private static final String DOG = "Dog";
    private static final String USERS = "Users";
    private static final String UPLOADS = "dogs/";
    public static String shareDog = "";

    private ImageView imAdd, imDog, imMyPro;
    private LinearLayout linearLayout;
    private View view;
    private TextView dogName, owenerName, breed, vetName, dateOfBirth;
    private TextView feedsInfoOver, feedsInfo, walksInfoOver, walksInfo;
    private View[] views = new View[4];
    private FirebaseDatabase database;
    private DatabaseReference dogRef, userRef;
    private StorageReference storageReference;
    private Button editBtn, shareBtn;

    public String email, keyId,share;
    public String myText;
    public String imageName;
    private int counter = 0, index = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        email = LogIn.email;
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(USERS);
        dogRef  = database.getReference(DOG);

        View v = inflater.inflate(R.layout.fragment_dog_profile, container, false);

        dogName         = v.findViewById(R.id.dogProfileName);
        owenerName      = v.findViewById(R.id.dogProfileOwnerTextview);
        breed           = v.findViewById(R.id.dogProfileBreedTextview);
        vetName         = v.findViewById(R.id.dogProfileVetTextview);
        dateOfBirth     = v.findViewById(R.id.dogProfileDateTextview);
        imDog           = v.findViewById(R.id.dogProfileImage);
        imMyPro         = v.findViewById(R.id.myProfile);
        editBtn         = v.findViewById(R.id.dogProfileEditBtn);
        shareBtn        = v.findViewById(R.id.dogProfileShareBtn);
        feedsInfo       = v.findViewById(R.id.feedNumber); //now how much he eat
        feedsInfoOver   = v.findViewById(R.id.feedOverNumber); //max of food
        walksInfo       = v.findViewById(R.id.walkNumber); //now how much he eat
        walksInfoOver   = v.findViewById(R.id.walkOverNumber); //max of walks

        imMyPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),PersonProfile.class);
                startActivity(intent);
            }
        });

        dogRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    if (MyDog.dogImage.equals(ds.child("imageName").getValue().toString())) {
                        owenerName.setText(ds.child("ownerName").getValue().toString());
                        dogName.setText(ds.child("dogName").getValue().toString());
                        imageName = ds.child("imageName").getValue(String.class);
                        breed.setText(ds.child("breed").getValue().toString());
                        vetName.setText(ds.child("vet").getValue().toString());
                        dateOfBirth.setText(ds.child("dateOfBirth").getValue().toString());
                        feedsInfo.setText(ds.child("currentFood").getValue().toString());
                        feedsInfoOver.setText(ds.child("maxFood").getValue().toString());
                        walksInfo.setText(ds.child("currentWalk").getValue().toString());
                        walksInfoOver.setText(ds.child("maxWalk").getValue().toString());
                        keyId = ds.getKey();
                        share = ds.child("share").getValue().toString();
                        shareDog = share;

                        MyDog.dogInUse = dogName.getText().toString();
                        storageReference = FirebaseStorage.getInstance().getReference().child(UPLOADS + imageName);
                        try {
                            File imgFile = File.createTempFile("profile", ".jpg");
                            storageReference.getFile(imgFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getPath());
                                    imDog.setImageBitmap(bitmap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),EditDogInfo.class);
                startActivity(intent);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder shareDialog = new AlertDialog.Builder(getActivity());
                shareDialog.setTitle("Enter email to share dog");

                final EditText emailShare = new EditText(getActivity());
                emailShare.setInputType(InputType.TYPE_CLASS_TEXT);
                shareDialog.setView(emailShare);

                shareDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myText = emailShare.getText().toString();
                        if(share.equals("")) {
                            dogRef.child(keyId).child("share").setValue(myText + ",");
                            Toast.makeText(getActivity(), dogName.getText() + " has been shared with " + myText, Toast.LENGTH_LONG).show();
                        }
                        else
                             {
                                 String shareString = share;
                                 ArrayList<String> shareList = new ArrayList<>(Arrays.asList(shareString.split(",")));
                                 boolean dogShare = false;
                                 for(String s : shareList){
                                     if(s.equals(myText)){
                                         dogShare =true;
                                     }
                                 }
                                 if(!dogShare){
                                     share += myText + ",";
                                     share = share.replace(",,",",");
                                     dogRef.child(keyId).child("share").setValue(share);
                                     Toast.makeText(getActivity(), dogName.getText() + " has been shared with " + myText, Toast.LENGTH_LONG).show();
                                 } else{
                                     Toast.makeText(getActivity(), dogName.getText() + " already shared with " + myText, Toast.LENGTH_LONG).show();
                                 }
                            }
                    }
                });

                shareDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                shareDialog.show();
            }
        });


        return v;
    }

}