package android.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri mImageUri;
    String myUrl;
    StorageTask storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference= FirebaseStorage.getInstance().getReference("Story");
        CropImage.activity().setAspectRatio(9,16).start(this);
    }

    /*
    private String getFileExtension(Uri uri){

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }*/

    private String getFileExtension(Uri uri){
        Log.i("uri", MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        Log.w("uri", uri.toString());
        return  MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    private void publishStory(){

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if(mImageUri != null){
            StorageReference imageRef = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

            storageTask = imageRef.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if(!task.isComplete()){
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(myid);
                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis()+86400000;
                        HashMap<String, Object >hashMap = new HashMap<>();
                        hashMap.put("imageUrl",myUrl);
                        hashMap.put("storyid",storyid);
                        hashMap.put("userid",myid);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend",timeend);
                        reference.child(storyid).setValue(hashMap);
                        pd.dismiss();
                        finish();
                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this , e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(AddStoryActivity.this , "no image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            mImageUri = activityResult.getUri();
            publishStory();
        }else{
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this , MainActivity.class));
            finish();
        }
    }
}