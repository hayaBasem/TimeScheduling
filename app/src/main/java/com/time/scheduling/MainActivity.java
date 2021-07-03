package com.time.scheduling;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.time.scheduling.DataBase.User;
import com.time.scheduling.DataBase.UserViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView forget_password;
    private AppCompatButton sign_in;
    private TextView sign_up;

    private TextInputLayout username_layout;
    private TextInputLayout password_layout;

    private UserViewModel userViewModel;
    private List<User> listUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                listUsers = users;
                for(User user : listUsers){
                    if(user.isLogin()){
                        Intent intent = new Intent(MainActivity.this,ListActivity.class);
                        intent.putExtra("userId",user.getId());
                        startActivity(intent);
                    }
                }
            }
        });
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        forget_password = findViewById(R.id.forget_password);
        sign_in = findViewById(R.id.sign_in);
        sign_up = findViewById(R.id.sign_up);
        username_layout =(TextInputLayout)findViewById(R.id.username_layout);
        password_layout =(TextInputLayout)findViewById(R.id.password_layout);

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
                username.setText("");
                password.setText("");
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().trim().equals("")){
                    username.setError("You must enter your name");
                    username_layout.setError("You must enter your name");
                    username_layout.setErrorIconDrawable(null);
                    return;
                }
                if(password.getText().toString().trim().equals("")){
                    password.setError("You must enter your password");
                    password_layout.setError("You must enter your password");
                    password_layout.setErrorIconDrawable(null);
                    return;
                }
                boolean isNotFound = true;
                for(User user : listUsers){
                    if(user.getUserName().equals(username.getText().toString().trim())
                       && user.getPassword().equals((password.getText().toString().trim()))){
                        Intent intent = new Intent(MainActivity.this,ListActivity.class);
                        intent.putExtra("userId",user.getId());
                        startActivity(intent);
                        username.setText("");
                        password.setText("");
                        user.setLogin(true);
                        userViewModel.update(user);
                        isNotFound = false;
                    }
                }
                if (isNotFound){
                    Toast.makeText(MainActivity.this,"Invalid User name or password",Toast.LENGTH_LONG).show();
                }

            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
                username.setText("");
                password.setText("");
            }
        });


    }
}