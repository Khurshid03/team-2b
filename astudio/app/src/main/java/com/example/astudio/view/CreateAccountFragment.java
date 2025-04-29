package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.astudio.databinding.FragmentCreateAccountBinding;

public class CreateAccountFragment extends Fragment implements CreateAccountUI {

    private FragmentCreateAccountBinding binding;
    private CreateAccountListener listener;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.CreateAccountButton.setOnClickListener(v -> {
            if (listener != null) {
                String username = binding.createAccountUsername.getText().toString().trim();
                String email = binding.CreateAccountEmail.getText().toString().trim();
                String password = binding.CreateAccountPassword.getText().toString().trim();
                listener.onCreateAccount(username, email, password, this);
            }
        });

        binding.ProceedToLoginButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProceedToLogin();
            }
        });
    }

    @Override
    public void setListener(CreateAccountListener listener) {
        this.listener = listener;
    }
}