package com.ayuget.redface.ui.view;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.ui.misc.SmileyAction;
import com.ayuget.redface.ui.misc.SmileyType;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileDetailsSmileyView extends CoordinatorLayout {
    @BindView(R.id.smiley_image)
    ImageView smileyImageView;

    @BindView(R.id.smiley_code)
    TextView smileyCodeView;

    @BindView(R.id.copy_smiley_code)
    ImageButton copySmileyCodeButton;

    @BindView(R.id.edit_smiley_keywords)
    ImageButton editSmileyKeywordsButton;

    @BindView(R.id.add_smiley_to_favorites)
    ImageButton addSmileyToFavoritesButton;

    @BindView(R.id.remove_smiley_from_favorites)
    ImageButton removeSmileyFromFavoritesButton;

    private final boolean isOwnProfile;
    private final Smiley smiley;
    private final SmileyType smileyType;
    private final OnSmileyActionPerformedListener onSmileyActionPerformedListener;

    public ProfileDetailsSmileyView(@NonNull Context context, boolean isOwnProfile, @NonNull Smiley smiley, SmileyType smileyType, OnSmileyActionPerformedListener onSmileyActionPerformedListener) {
        super(context);
        this.isOwnProfile = isOwnProfile;
        this.smiley = smiley;
        this.smileyType = smileyType;
        this.onSmileyActionPerformedListener = onSmileyActionPerformedListener;
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.item_profile_custom_smiley, this);
        ButterKnife.bind(this);

        Glide.with(this).load(smiley.imageUrl()).into(smileyImageView);
        smileyCodeView.setText(smiley.code());

        TooltipCompat.setTooltipText(copySmileyCodeButton, getContext().getString(R.string.profile_personal_smilies_copy_code));
        TooltipCompat.setTooltipText(editSmileyKeywordsButton, getContext().getString(R.string.profile_personal_smilies_edit_smiley_keywords));

        copySmileyCodeButton.setOnClickListener((click) -> onSmileyActionPerformedListener.onSmileyActionPerformed(smiley, SmileyAction.COPY_CODE_TO_CLIPBOARD));
        editSmileyKeywordsButton.setOnClickListener((click) -> onSmileyActionPerformedListener.onSmileyActionPerformed(smiley, SmileyAction.EDIT_KEYWORDS));
        addSmileyToFavoritesButton.setOnClickListener((click) -> onSmileyActionPerformedListener.onSmileyActionPerformed(smiley, SmileyAction.ADD_TO_FAVORITES));
        removeSmileyFromFavoritesButton.setOnClickListener((click) -> onSmileyActionPerformedListener.onSmileyActionPerformed(smiley, SmileyAction.REMOVE_FROM_FAVORITES));

        editSmileyKeywordsButton.setVisibility(GONE); // Not ready yet

        if (smileyType == SmileyType.PERSONAL) { // Personal smilies are necessarily in the favorites
            if (isOwnProfile) { // Personal smilies are necessarily in the favorites
                addSmileyToFavoritesButton.setVisibility(GONE);
            }

            removeSmileyFromFavoritesButton.setVisibility(GONE);
        }

        if (isOwnProfile) {
            addSmileyToFavoritesButton.setVisibility(GONE);
        }
    }

    public interface OnSmileyActionPerformedListener {
        void onSmileyActionPerformed(Smiley smiley, SmileyAction smileyAction);
    }
}
