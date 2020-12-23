package android.example.instagramclone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class Posting extends AppCompatActivity implements View.OnClickListener {
    Button next, prev,add,upload;
    LinearLayout LL;

    public static ArrayList<Uri> imageuris;
    public ImageSwitcher image;
    public static final int PICK = 0;
    int position = 0;
    int pics=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        image = findViewById(R.id.Image);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        LL = findViewById(R.id.layout);
        add = findViewById(R.id.addbtn);
        add.setOnClickListener(this::onClick);
        imageuris = new ArrayList<>();
        upload=findViewById(R.id.upload);

        upload.setOnClickListener(new View.OnClickListener() {
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


    public void onClick(View v) {
        addView1();
    }

    public void addView1() {
        final View add_ingredients_row = getLayoutInflater().inflate(R.layout.quantity,null,false);
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
    public void removeView(View v){
        LL.removeView(v);
    }

}