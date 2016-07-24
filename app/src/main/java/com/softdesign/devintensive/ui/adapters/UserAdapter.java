package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.helper.ItemTouchHelperAdapter;
import com.softdesign.devintensive.utils.helper.ItemTouchHelperViewHolder;
import com.softdesign.devintensive.utils.helper.OnStartDragListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements ItemTouchHelperAdapter {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UsersAdapter";

    private Context mContext;
    private List<User> mUsers;
    private UserViewHolder.CustomClickListener mCustomClickListener;

    private final OnStartDragListener mDragStartListener;

    public UserAdapter(List<User> users, UserViewHolder.CustomClickListener customClickListener, OnStartDragListener dragStartListener) {
        mUsers = users;
        mCustomClickListener = customClickListener;
        mDragStartListener = dragStartListener;
    }

    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);

        return new UserViewHolder(convertView, mCustomClickListener);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBindViewHolder(final UserAdapter.UserViewHolder holder, int position) {
        final User user = mUsers.get(position);
        final String userPhoto;

        if (user.getPhoto().isEmpty()) {
            userPhoto = "null";
            Log.e(TAG, "onBindViewHolder: user with name " + user.getFullName() + " has empty name");
        } else {
            userPhoto = user.getPhoto();
        }

        // Загрузка фото пользователей из списка
        DataManager.getInstance().getPicasso()
                .load(userPhoto)
                .placeholder(holder.mDummy)
                .error(holder.mDummy)
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.userPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "load photo from cache");
                    }

                    @Override
                    public void onError() {
                        DataManager.getInstance().getPicasso()
                                .load(userPhoto)
                                .placeholder(holder.mDummy)
                                .error(holder.mDummy)
                                .fit()
                                .centerCrop()
                                .into(holder.userPhoto, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "load photo from network");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, "Could not fetch image");
                                    }
                                });
                    }
                });

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getRating()));
        holder.mCodeLines.setText(String.valueOf(user.getCodeLines()));
        holder.mProjects.setText(String.valueOf(user.getProjects()));

        if (user.getBio() == null || user.getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getBio());
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        User user1 = mUsers.get(fromPosition);
        User user2 = mUsers.get(toPosition);
        long tmpSortId = user1.getSortId();
        user1.setSortId(user2.getSortId());
        user2.setSortId(tmpSortId);
        user1.update();
        user2.update();

        Collections.swap(mUsers, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mUsers.get(position).delete();

        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {
        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio;
        protected Button mShowMore;
        protected Drawable mDummy;

        private CustomClickListener mListener;

        @SuppressWarnings("deprecation")
        public UserViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);
            mListener = customClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_txt);
            mRating = (TextView) itemView.findViewById(R.id.rating_txt);
            mCodeLines = (TextView) itemView.findViewById(R.id.code_lines_txt);
            mProjects = (TextView) itemView.findViewById(R.id.projects_txt);
            mBio = (TextView) itemView.findViewById(R.id.bio_txt);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);

            mDummy = userPhoto.getContext().getResources().getDrawable(R.drawable.user_bg);

            mShowMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onUserItemClickListener(getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        public interface CustomClickListener {
            void onUserItemClickListener(int position);
        }
    }
}
