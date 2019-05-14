package prashantkumar.com.getactive;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmailField;
    private EditText regPassField;
    private EditText regConfirmPassField;

    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar regProgressBar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        regEmailField = (EditText) findViewById(R.id.reg_email);
        regPassField = (EditText) findViewById(R.id.reg_password);
        regConfirmPassField = (EditText) findViewById(R.id.reg_confirm_password);

        regBtn = (Button) findViewById(R.id.reg_btn);
        regLoginBtn = (Button)findViewById(R.id.reg_login_btn);

        regProgressBar = (ProgressBar) findViewById(R.id.reg_progress_bar);

        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = regEmailField.getText().toString();
                String pass = regPassField.getText().toString();
                String confirmpass = regConfirmPassField.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmpass)){

                    if(pass.equals(confirmpass)){

                        regProgressBar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                }else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error:" + errorMessage,Toast.LENGTH_LONG).show();




                                }

                                regProgressBar.setVisibility(View.INVISIBLE);




                            }
                        });



                    }else{

                        Toast.makeText(RegisterActivity.this,"Your Password and Confirm Password doesn't match",Toast.LENGTH_LONG).show();
                    }


                }


            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){

            sendToMainACtivity();
        }



    }

    private void sendToMainACtivity() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();


    }
}

