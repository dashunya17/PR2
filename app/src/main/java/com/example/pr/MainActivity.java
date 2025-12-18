package com.example.pr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdminLoginDialogFragment.LoginDialogListener {
    private Button  btnAdmin;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseHelper = new FirebaseHelper();
        btnAdmin = findViewById(R.id.button);



        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAdminLoginDialog();
                }
            });
        } else {
            Toast.makeText(this, "Кнопка администратора не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAdminLoginDialog() {
        try {
            AdminLoginDialogFragment dialog = new AdminLoginDialogFragment();
            dialog.setLoginDialogListener(this);
            dialog.show(getSupportFragmentManager(), "AdminLoginDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при открытии диалога: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Доступ разрешён", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailure() {
        Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
    }
}