package com.ayuget.redface.ui.view;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.AccountActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileConnectionStatusView extends ConstraintLayout {
    @BindView(R.id.connection_action_button)
    Button connectionActionButton;

    public ProfileConnectionStatusView(Context context) {
        super(context);

        inflate(getContext(), R.layout.item_profile_connection_status, this);
        ButterKnife.bind(this);

        connectionActionButton.setOnClickListener((click) -> {
            Intent intent = new Intent(getContext(), AccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(UIConstants.ARG_RELOGIN_MODE, true);
            getContext().startActivity(intent);
        });
    }
}
