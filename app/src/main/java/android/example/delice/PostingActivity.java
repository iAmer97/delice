package android.example.delice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostingActivity extends AppCompatActivity implements View.OnClickListener {
    public int stepNum = 0;

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    Button add, add2, upload, post;
    LinearLayout LL, LL2;
    String myUrl = "";
    boolean ingredientChecker = false;
    boolean stepsChecker = false;
    int j = 0;
    ImageView cancel;
    EditText description, name, numberOfServings, tagsField;

    Map<String, Object> ingredients, stepsMap, tags;

    List<Object> imageDownloadUrls;

    public static ArrayList<Uri> imageuris;
    public ImageView image;
    public static final int PICK = 0;
    int position = 0;
    int pics = 0;
    String postid;
    String editTitle,editDescription, editImage;

    StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        image = findViewById(R.id.Image);
        LL = findViewById(R.id.layout);
        LL2 = findViewById(R.id.layout2);
        add = findViewById(R.id.addbtn);
        add2 = findViewById(R.id.addbtn2);
        add2.setOnClickListener(this::onClick2);
        add.setOnClickListener(this::onClick);
        imageuris = new ArrayList<>();
        imageDownloadUrls = new ArrayList<>();
        ingredients = new HashMap();
        stepsMap = new HashMap();
        tags = new HashMap();
        upload = findViewById(R.id.upload);
        post = findViewById(R.id.post);
        name = findViewById(R.id.name);
        description = findViewById(R.id.RecDes);
        numberOfServings = findViewById(R.id.numberOfServings);
        tagsField = findViewById(R.id.tags);
        cancel = findViewById(R.id.cancelPosting);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String key = intent.getStringExtra("key");


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        if(key.equalsIgnoreCase("edit")){
            postid = intent.getStringExtra("postid");
            upload.setText("update");
            loadPostData(postid);
        }
        else {
            postid = reference.push().getKey();
            upload.setText("upload");
        }
        storageReference = FirebaseStorage.getInstance().getReference("posts/" + postid);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });

//        image.setFactory(new ViewSwitcher.ViewFactory() {
//            @Override
//            public View makeView() {
//                ImageView imageView = new ImageView(getApplicationContext());
//                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                return imageView;
//            }
//        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] texts = new String[]{name.getText().toString(), description.getText().toString(), numberOfServings.getText().toString(), tagsField.getText().toString()};
                if (check(texts,stepsMap,ingredients)) {
                    uploadImage();
                }
            }
        });
    }

    private void loadPostData(String postid) {

        Log.w("postdata","hi");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name.setText(""+snapshot.child("name").getValue());
                numberOfServings.setText(""+snapshot.child("numberOfServings").getValue());
                description.setText(""+snapshot.child("description").getValue());

                String tagsString="";
                for(DataSnapshot dataSnapshot : snapshot.child("tags").getChildren()){
                    tagsString+= dataSnapshot.getKey() + " ";
                }
                tagsField.setText(tagsString);

                for(DataSnapshot dataSnapshot : snapshot.child("ingredients").getChildren()){
                    String quantity = dataSnapshot.getValue()+"";
                    String ingredient = dataSnapshot.getKey();

                    addView1(quantity,ingredient);
                }

                for (int i = 0; i<snapshot.child("steps").getChildrenCount();i++){
                    String description = ""+snapshot.child("steps").child("step Step "+(i+1)).getValue();
                    addview2(description);
                }

                Glide.with(getBaseContext()).load(snapshot.child("postimages/0").getValue()+"").into(image);
                imageDownloadUrls.add(snapshot.child("postimages/0").getValue()+"");
                //postimage

                Log.w("postdata",snapshot.toString());
                Log.w("postdata",snapshot.child("postimages/0").getValue()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFileExtension(Uri uri) {
        Log.i("uri", MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        Log.w("uri", uri.toString());
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        if(getIntent().getStringExtra("key").equalsIgnoreCase("edit")){
            progressDialog.setMessage("Updatng");
        }
        else {
            progressDialog.setMessage("Posting");
        }
        progressDialog.show();

        String[] tagsList = tagsField.getText().toString().split(" ");
        for (int i = 0; i < tagsList.length; i++) {
            Log.w("tags",tagsList[i]);
            tags.put(tagsList[i], true);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tags").child(tagsList[i]);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        reference.setValue(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        for (int i = 0; i < LL.getChildCount(); i++) {
            View ingredientView = LL.getChildAt(i);

            EditText quantity = ingredientView.findViewById(R.id.quantity);
            EditText ingredient = ingredientView.findViewById(R.id.ingredient);

            Log.w("ingredients",ingredient.getText().toString());

            if (quantity.getText().toString() == "" || ingredient.getText().toString() == "") {
                ingredientChecker = true;
                break;
            } else {
                ingredients.put(ingredient.getText().toString(), quantity.getText().toString());
            }
        }

        for (int i = 0; i < LL2.getChildCount(); i++) {
            View stepView = LL2.getChildAt(i);
            Log.i("steps", LL2.getChildCount() + "");
            TextView steps = stepView.findViewById(R.id.steps);
            EditText desc = stepView.findViewById(R.id.desc);

            Log.w("steps",desc.getText().toString());

            if (steps.getText().toString() == "" || desc.getText().toString() == "") {
                stepsChecker = true;
                break;
            } else {
                stepsMap.put("step " + steps.getText().toString(), desc.getText().toString());
            }
        }

        if (imageuris.size() != 0) {

            for (j = 0; j < imageuris.size(); j++) {
                myUrl = "";
                StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageuris.get(j)));

                uploadTask = filereference.putFile(imageuris.get(j));
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filereference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();
                            Log.w("urls", myUrl);
                            if(getIntent().getStringExtra("key").equalsIgnoreCase("edit")){
                                imageDownloadUrls.set(0,myUrl);
                            }
                            else{
                                imageDownloadUrls.add(myUrl);
                            }

                            if (imageDownloadUrls.size() == imageuris.size()) {
                                HashMap<String, Object> hashMap = new HashMap<>();

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");


                                hashMap.put("postid", postid);
                                hashMap.put("name", name.getText().toString());
                                hashMap.put("postimages", imageDownloadUrls);
                                hashMap.put("description", description.getText().toString());
                                hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap.put("tags", tags);
                                hashMap.put("ingredients", ingredients);
                                hashMap.put("steps", stepsMap);
                                hashMap.put("numberOfServings", numberOfServings.getText().toString());
                                reference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            if(getIntent().getStringExtra("key").equalsIgnoreCase("edit")) {
                                                Toast.makeText(PostingActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                            startActivity(new Intent(PostingActivity.this, MainActivity.class));
                                            finish();
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            if(getIntent().getStringExtra("key").equalsIgnoreCase("edit")) {
                                                Toast.makeText(PostingActivity.this, "Something went wrong during updating post", Toast.LENGTH_SHORT).show();
                                            }
                                            startActivity(new Intent(PostingActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }
                                });

                            }
                        } else {
                            Toast.makeText(PostingActivity.this, "Image number " + j + "failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostingActivity.this, "[" + j + "]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


        } else {
            HashMap<String, Object> hashMap = new HashMap<>();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");


            hashMap.put("postid", postid);
            hashMap.put("name", name.getText().toString());
            hashMap.put("postimages", imageDownloadUrls);
            hashMap.put("description", description.getText().toString());
            hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("tags", tags);
            hashMap.put("ingredients", ingredients);
            hashMap.put("steps", stepsMap);
            hashMap.put("numberOfServings", numberOfServings.getText().toString());

            reference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(PostingActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostingActivity.this, MainActivity.class));
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(PostingActivity.this, "Something went wrong during updating post", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostingActivity.this, MainActivity.class));
                        finish();
                    }
                }
            });


        }
    }

    public void pickImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK);
    }

    public int stepNumInc() {
        int num = getStepNum();
        num++;
        setStepNum(num);
        return num;
    }

    public void stepNumDec() {
        int num = getStepNum();
        num--;
        setStepNum(num);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK) {

            if (resultCode == Activity.RESULT_OK) {

                if (data.getClipData() != null) {

                    int n = data.getClipData().getItemCount();
                    for (int i = 0; i < n; i++) {

                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageuris.add(imageUri);

                    }
                    image.setImageURI(imageuris.get(0));
                    position = 0;
                } else {
                    Uri imageUri = data.getData();
                    imageuris.add(imageUri);
                    image.setImageURI(imageuris.get(0));
                    position = 0;
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        addView1();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void addView1() {
        final View add_ingredients_row = getLayoutInflater().inflate(R.layout.quantity, null, false);
        EditText quantity = add_ingredients_row.findViewById(R.id.quantity);
        EditText ingredient = add_ingredients_row.findViewById(R.id.ingredient);
        ImageView cancel = add_ingredients_row.findViewById(R.id.cancelbtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(add_ingredients_row);
            }
        });
        LL.addView(add_ingredients_row);
    }

    public void addView1(String number, String thing) {
        final View add_ingredients_row = getLayoutInflater().inflate(R.layout.quantity, null, false);
        EditText quantity = add_ingredients_row.findViewById(R.id.quantity);
        quantity.setText(number);
        EditText ingredient = add_ingredients_row.findViewById(R.id.ingredient);
        ingredient.setText(thing);
        ImageView cancel = add_ingredients_row.findViewById(R.id.cancelbtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(add_ingredients_row);
            }
        });
        LL.addView(add_ingredients_row);
    }

    public void removeView(View v) {
        LL.removeView(v);
    }


    public void onClick2(View v) {
        addview2();
    }

    private void addview2() {
        final View add_steps = getLayoutInflater().inflate(R.layout.steps, null, false);
        TextView steps = add_steps.findViewById(R.id.steps);
        steps.setText(steps.getText() + "" + stepNumInc());
        EditText desc = add_steps.findViewById(R.id.desc);
        ImageView cancel2 = add_steps.findViewById(R.id.cancelbtn2);
        cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView2(add_steps);
            }
        });
        LL2.addView(add_steps);
    }

    private void addview2(String description) {
        final View add_steps = getLayoutInflater().inflate(R.layout.steps, null, false);
        TextView steps = add_steps.findViewById(R.id.steps);
        steps.setText(steps.getText() + "" + stepNumInc());
        EditText desc = add_steps.findViewById(R.id.desc);
        desc.setText(description);
        ImageView cancel2 = add_steps.findViewById(R.id.cancelbtn2);
        cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView2(add_steps);
            }
        });
        LL2.addView(add_steps);
    }

    public void removeView2(View v) {
        LL2.removeView(v);
        stepNumDec();
    }

    public boolean check(String[] arr,Map stepsMap,Map ingredients) {
        for (String text : arr) {
            if (text.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (LL2.getChildCount() == 0 || LL.getChildCount() == 0){
            Toast.makeText(this, "Please fill the steps and ingredients", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(imageuris.size() == 0 && !getIntent().getStringExtra("key").equalsIgnoreCase("edit")){
            Toast.makeText(this, "Please upload a photo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}