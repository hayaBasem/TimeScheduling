package com.time.scheduling;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.time.scheduling.DataBase.User;
import com.time.scheduling.DataBase.UserViewModel;

import java.util.Calendar;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class SignUpActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private List<User> listUsers;
    AppCompatButton sign_up;
    Spinner spinner;
    String[] spinnerValue = {
            "What were the last four digits of your telephone number?",
            "What primary school did you attend?",
            "What is your spouse or partner's mother's maiden name?",
            "What day of the day were you born?(dd-mm-yyyy)"
    };


    private EditText username;
    private EditText password;
    private EditText answer;
    private TextInputLayout username_layout;
    private TextInputLayout password_layout;
    private TextInputLayout answer_layout;
    private GifImageView gifImageView;

    private boolean isUserExist = false;
    private boolean isPasswordCheck = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                listUsers = users;
                Log.e("listUsers",listUsers.size()+"");
            }
        });
        sign_up = findViewById(R.id.sign_up);
        spinner =(Spinner)findViewById(R.id.security_question);

        username =(EditText)findViewById(R.id.username);
        password =(EditText)findViewById(R.id.password);
        answer =(EditText)findViewById(R.id.answer);

        username_layout =(TextInputLayout)findViewById(R.id.username_layout);
        password_layout =(TextInputLayout)findViewById(R.id.password_layout);
        answer_layout =(TextInputLayout)findViewById(R.id.answer_layout);

        gifImageView =(GifImageView)findViewById(R.id.gifImageView);
        gifImageView.setVisibility(View.INVISIBLE);
        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUpActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                                if(month < 10 &&  day < 10){
                                    answer.setText("0"+(month+1)+"/0"+datePicker.getDayOfMonth()+"/"+year);
                                }else if (month < 10){
                                    answer.setText("0"+(month+1)+"/"+datePicker.getDayOfMonth()+"/"+year);
                                }else if(day < 10){
                                    answer.setText((month+1)+"/0"+datePicker.getDayOfMonth()+"/"+year);
                                }else {
                                    answer.setText((month+1)+"/"+datePicker.getDayOfMonth()+"/"+year);
                                }

                            }
                        }, 1999, Calendar.getInstance().get(Calendar.MONTH), 1).show();
            }
        });


        SecurityAdapter adapter = new SecurityAdapter(SignUpActivity.this, android.R.layout.simple_list_item_1);
        adapter.addAll(spinnerValue);
        adapter.add("Security Question");
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                gifImageView.setVisibility(View.INVISIBLE);
                answer.setEnabled(true);
                answer.setText("");
                switch (spinner.getSelectedItem().toString()){
                    case "What were the last four digits of your telephone number?" :
                        answer.setInputType(InputType.TYPE_CLASS_NUMBER);
                        answer_layout.setHint("last 4 digits Eg. 05***3577");
                        break;
                    case "What primary school did you attend?" :
                        answer.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                        answer_layout.setHint("Answer Security");
                        break;
                    case "What is your spouse or partner's mother's maiden name?"  :
                        answer.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                        answer_layout.setHint("Answer Security");
                        break;
                    case "What day of the day were you born?(dd-mm-yyyy)" :
                        answer.setInputType(InputType.TYPE_CLASS_NUMBER);
                        answer_layout.setHint("MM/DD/YYYY E.g 01/30/1999");
                        gifImageView.setVisibility(View.VISIBLE);
                        answer.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUserExist = false;
                username_layout.setError(null);
                username.setError(null);
                for (User user : listUsers) {
                    if (user.getUserName().equals(username.getText().toString())){
                        username_layout.setError("This Account Is Exist");
                        isUserExist = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isPasswordCheck = false;
                password_layout.setError("");
                if(password.getText().toString().length() < 6){
                    password_layout.setHelperText("The Password must be at least 6 character or Numbers");
                    isPasswordCheck = false;
                }else {
                    isPasswordCheck = true;
                    password_layout.setHelperText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                switch (spinner.getSelectedItem().toString()){
                    case "What day of the day were you born?(dd-mm-yyyy)" :

                        break;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().trim().equals("")){
                    username.setError("You must enter your name");
                    username_layout.setError("You must enter your name");
                    username_layout.setErrorIconDrawable(null);
                    return;
                }
                if(username.getText().toString().trim().length()<3){
                    username.setError("You must enter your full name");
                    username_layout.setError("You must enter your full name");
                    username_layout.setErrorIconDrawable(null);
                    return;
                }
                if(isUserExist){
                    username.setError("This Account is Exist");
                    username_layout.setError("This Account is Exist");
                    username_layout.setErrorIconDrawable(null);
                    return;
                }
                if(password.getText().toString().trim().length()<6){
                    password.setError("The Password must be at least 6 character or Numbers");
                    password_layout.setError("The Password must be at least 6 character or Numbers");
                    password_layout.setErrorIconDrawable(null);
                    return;
                }
                if(answer.getText().toString().trim().length() == 0){
                    answer.setError("You must enter your answer");
                    answer_layout.setError("You must enter your answer");
                    answer_layout.setErrorIconDrawable(null);
                    return;
                }
                switch (spinner.getSelectedItem().toString()){
                    case "What were the last four digits of your telephone number?" :
                        if(answer.getText().toString().trim().length() < 4){
                            answer.setError("You must enter last 4 digits of your telephone number");
                            answer_layout.setError("You must enter last 4 digits of your telephone number");
                            answer_layout.setErrorIconDrawable(null);
                            return;
                        }
                        break;
                    case "What primary school did you attend?" :
                        if(answer.getText().toString().trim().length() < 4){
                            answer.setError("You must enter full name primary school");
                            answer_layout.setError("You must enter full name primary school");
                            answer_layout.setErrorIconDrawable(null);
                            return;
                        }
                        break;
                    case "What is your spouse or partner's mother's maiden name?"  :
                        if(answer.getText().toString().trim().length() < 3){
                            answer.setError("You must enter full name your spouse or partner's mother's maiden name");
                            answer_layout.setError("You must enter your spouse or partner's mother's maiden name");
                            answer_layout.setErrorIconDrawable(null);
                            return;
                        }
                        break;
                    case "What day of the day were you born?(dd-mm-yyyy)" :
                        if(answer.getText().toString().trim().length() == 0){
                            Toast.makeText(SignUpActivity.this,"You must choose date click the icon",Toast.LENGTH_LONG).show();
                            return;
                        }
                        break;
                }
                User user = new User();
                user.setUserName(username.getText().toString().trim());
                user.setPassword(password.getText().toString().trim());
                user.setQuestionNumber(spinner.getId());
                user.setAnswerQuestion(answer.getText().toString().trim());
                user.setLogin(true);
                userViewModel.insert(user);
                onBackPressed();
            }
        });

    }
}