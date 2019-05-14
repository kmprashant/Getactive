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

public class LoginActivity extends AppCompatActivity {
    private EditText edtLoginEmail;
    private EditText edtLoginPassword;
    private Button loginBtn;
    private Button loginRegBtn;
    private FirebaseAuth mAuth;
    private ProgressBar loginprogressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        edtLoginEmail = (EditText) findViewById(R.id.reg_email);
        edtLoginPassword = (EditText) findViewById(R.id.reg_confirm_password);

        loginBtn = (Button) findViewById(R.id.reg_btn);
        loginRegBtn = (Button)findViewById(R.id.loginRegBtn);

        loginprogressbar  = (ProgressBar) findViewById(R.id.loginProgressBar);

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);

            }
        });



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail = edtLoginEmail.getText().toString();
                String loginPassword = edtLoginPassword.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
                    loginprogressbar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                sendToMain();


                            }else{

                                String errorMessae = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error :" +errorMessae , Toast.LENGTH_LONG).show();

                            }

                            loginprogressbar.setVisibility(View.INVISIBLE);


                        }
                    });

                }

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){

            sendToMain();


        }else{



        }




    }

    private void sendToMain() {

        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
