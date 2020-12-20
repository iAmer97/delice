package android.example.instagramclone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.example.instagramclone.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class Posting extends AppCompatActivity {
    public ImageSwitcher image;
    Button next, prev;

    public ArrayList<Uri> imageuris;
    public static final int PICK = 0;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        image = findViewById(R.id.Image);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        imageuris = new ArrayList<>();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position < imageuris.size() - 1){
                    position++;
                    image.setImageURI(imageuris.get(position));
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position > 0){
                    position--;
                    image.setImageURI(imageuris.get(position));
                }

            }
        });
        image.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                return imageView;
            }
        });


    }

    public void pickImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK);

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
}