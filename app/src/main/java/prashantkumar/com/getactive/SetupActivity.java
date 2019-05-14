package prashantkumar.com.getactive;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static prashantkumar.com.getactive.Manifest.*;

public class SetupActivity extends AppCompatActivity {
    private Toolbar setupToolbar;
    private Uri mainImageUri=null;
    private EditText setupNameText;
    private Button setupBtn;
    private String user_id;
    private boolean isChanged = false;
    private FirebaseFirestore firebaseFirestore;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupProgressbar;

    private CircleImageView setupImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupNameText =  findViewById(R.id.setup_name);
        setupBtn =  findViewById(R.id.setup_btn);
        setupProgressbar = findViewById(R.id.setup_progress);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupProgressbar.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);


        //This below few line codes are for retrieving data from firestore to load image into the account setting setup
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        setupNameText.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }else {
                        Toast.makeText(SetupActivity.this,"Data Doesn't exists" ,Toast.LENGTH_LONG).show();


                    }

                }else{
                    String firestoreerror = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStore Retrieve Error" + firestoreerror,Toast.LENGTH_LONG).show();


                }
                setupProgressbar.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_name = setupNameText.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {

                    setupProgressbar.setVisibility(View.VISIBLE);
                    if(isChanged) {



                        user_id = firebaseAuth.getCurrentUser().getUid();


                        final StorageReference image_path = storageReference.child("Profile_images").child(user_id + "jpg");
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();
                                            storeToFireStore(task,downloadUrl, user_name);

                                        }
                                    });

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error" + error, Toast.LENGTH_LONG).show();
                                    setupProgressbar.setVisibility(View.INVISIBLE);

                                }

                            }
                        });

                    }else {

                        storeToFireStore(null,mainImageUri.toString(),user_name);


                    }                }
            }
        });


        setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupImage= (CircleImageView) findViewById(R.id.setupImage);
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},1);


                    }else {


                        BringImagePicker();
                    }
                }else{

                    BringImagePicker();
                }

            }
        });
    }

    private void storeToFireStore(@NonNull Task<UploadTask.TaskSnapshot> task,String uri,String user_name) {

        String download_uri;
        if(task!=null) {
            download_uri =uri;

        }else {
            download_uri = mainImageUri.toString();
        }
        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",download_uri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this,"User Settings are Updated",Toast.LENGTH_LONG).show();

                    Intent mainActivity = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(mainActivity);

                }else
                {
                    String firestoreerror = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStoreError" + firestoreerror,Toast.LENGTH_LONG).show();

                }
                setupProgressbar.setVisibility(View.INVISIBLE);

            }
        });
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged =true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
}
