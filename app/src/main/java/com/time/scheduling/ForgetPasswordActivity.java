package com.time.scheduling;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.time.scheduling.DataBase.User;
import com.time.scheduling.DataBase.UserViewModel;

import java.util.Calendar;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class ForgetPasswordActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private List<User> listUsers;

    AppCompatButton send;
    Spinner spinner;
    String[] spinnerValue = {
            "What were the last four digits of your telephone number?",
            "What primary school did you attend?",
            "What is your spouse or partner's mother's maiden name?",
            "What day of the day were you born?(dd-mm-yyyy)"
    };

    private EditText username;
    private EditText answer;
    private TextInputLayout username_layout;
    private TextInputLayout answer_layout;
    private GifImageView gifImageView;
    private LinearLayout liner_qustion;
    private LinearLayout liner_rest;

    private EditText password;
    private EditText confirm_password;
    private TextInputLayout password_layout;
    private TextInputLayout confirm_password_layout;
    private boolean isUserExist = false;

    private User findUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                listUsers = users;
                Log.e("listUsers",listUsers.size()+"");
            }
        });
        send = findViewById(R.id.send);
        spinner =(Spinner)findViewById(R.id.security_question);

        username =(EditText)findViewById(R.id.username);
        answer =(EditText)findViewById(R.id.answer);

        username_layout =(TextInputLayout)findViewById(R.id.username_layout);
        answer_layout =(TextInputLayout)findViewById(R.id.answer_layout);

        liner_qustion =(LinearLayout)findViewById(R.id.liner_qustion);
        liner_rest =(LinearLayout)findViewById(R.id.liner_rest);

        password =(EditText)findViewById(R.id.password);
        confirm_password =(EditText)findViewById(R.id.confirm_password);
        password_layout =(TextInputLayout)findViewById(R.id.password_layout);
        confirm_password_layout =(TextInputLayout)findViewById(R.id.confirm_password_layout);

        gifImageView =(GifImageView)findViewById(R.id.gifImageView);
        gifImageView.setVisibility(View.INVISIBLE);
        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ForgetPasswordActivity.this,
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

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password_layout.setError("");
                if(password.getText().toString().length() < 6){
                    password_layout.setHelperText("The Password must be at least 6 character or Numbers");
                }else {
                    password_layout.setHelperText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(confirm_password.getText().toString().equals(password.getText().toString())){
                    confirm_password.setCompoundDrawables(null,null, ContextCompat.getDrawable(ForgetPasswordActivity.this, R.drawable.ic_check_circle),null);
                }else {
                    confirm_password.setCompoundDrawables(null,null,null,null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SecurityAdapter adapter = new SecurityAdapter(ForgetPasswordActivity.this, android.R.layout.simple_list_item_1);
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
                        isUserExist = true;
                        findUser = user;
                    }
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
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(send.getText().equals("Rest password")){
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
                    if(!isUserExist){
                        username.setError("This Account is Not Exist");
                        username_layout.setError("This Account is Not Exist");
                        username_layout.setErrorIconDrawable(null);
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
                                Toast.makeText(ForgetPasswordActivity.this,"You must choose date click the icon",Toast.LENGTH_LONG).show();
                                return;
                            }
                            break;
                    }
                    Log.e("user",findUser.getUserName()+" | "+isUserExist);
                    if(isUserExist && findUser != null){
                        if(findUser.getAnswerQuestion().equals(answer.getText().toString().trim())){
                            liner_qustion.setVisibility(View.INVISIBLE);
                            liner_rest.setVisibility(View.VISIBLE);
                            send.setText("Rest");
                        }else {
                            Toast.makeText(ForgetPasswordActivity.this,"This answer is not true",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }else {
                    if(password.getText().toString().trim().length()<6){
                        password.setError("The Password must be at least 6 character or Numbers");
                        password_layout.setError("The Password must be at least 6 character or Numbers");
                        password_layout.setErrorIconDrawable(null);
                        return;
                    }
                    if(confirm_password.getText().toString().equals(password.getText().toString())){
                        findUser.setPassword(password.getText().toString().trim());
                        userViewModel.update(findUser);
                        onBackPressed();
                    }else {
                        Toast.makeText(ForgetPasswordActivity.this,"You must Confirm Password True",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }
}