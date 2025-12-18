package com.example.pr;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AdminLoginDialogFragment extends DialogFragment {

    private EditText etLogin, etPassword;
    private FirebaseHelper firebaseHelper;

    // Интерфейс для callback
    public interface LoginDialogListener {
        void onLoginSuccess();
        void onLoginFailure();
    }

    private LoginDialogListener listener;

    public void setLoginDialogListener(LoginDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        firebaseHelper = new FirebaseHelper();

        // Создаём AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Получаем layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_admin, null);

        // Находим элементы в layout
        etLogin = dialogView.findViewById(R.id.dialog_et_login);
        etPassword = dialogView.findViewById(R.id.dialog_et_password);
        Button btnLogin = dialogView.findViewById(R.id.dialog_btn_login);
        Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        // Обработка кнопки Войти
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Обработка кнопки Отмена
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(dialogView)
                .setTitle("Вход администратора");

        return builder.create();
    }

    private void attemptLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.checkAdminCredentials(login, password,
                new FirebaseHelper.OnCheckCredentialsListener() {
                    @Override
                    public void onCredentialsChecked(boolean isValid) {
                        if (isValid) {
                            // Успешный вход
                            if (listener != null) {
                                listener.onLoginSuccess();
                            }
                            dismiss(); // Закрываем диалог

                            // Переход на AdminActivity
                            Intent intent = new Intent(getActivity(), MainActivity2.class);
                            intent.putExtra("ADMIN_LOGIN", login);
                            startActivity(intent);

                            // Закрываем текущую активность, если нужно
                            // getActivity().finish();

                        } else {
                            // Неверные данные
                            Toast.makeText(getActivity(),
                                    "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onLoginFailure();
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getActivity(),
                                "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}