package com.vitalsync.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.ui.CommonPopupView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GuideFragment extends Fragment {

    private Button btnConfirm;
    private TextView privacyPolicyView;
    private CommonPopupView webviewPopupView;
    private WebView privacyWebView;
    private ImageButton webviewCloseButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, container, false);
        btnConfirm = view.findViewById(R.id.btn_guide_confirm);
        privacyPolicyView = view.findViewById(R.id.view_login_policy);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new LoginFragment());
            }
        });

        View viewWebViewContainer = inflater.inflate(R.layout.view_webview_container,container, false);
        privacyWebView = viewWebViewContainer.findViewById(R.id.view_webview);
        privacyWebView.getSettings().setJavaScriptEnabled(true);
        privacyWebView.loadUrl(Config.PRIVACY_POLICY);
        webviewCloseButton = viewWebViewContainer.findViewById(R.id.view_webview_close);
        webviewPopupView = new CommonPopupView(requireContext(), viewWebViewContainer);

        privacyPolicyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //개인정보 처리방침 webview
                webviewPopupView.show();
            }
        });
        webviewCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webviewPopupView.dismiss();
            }
        });

        return view;
    }
}
