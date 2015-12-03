package com.assignment.sjsu.hudoassenco.cmpe137;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CollaboratorsAdapter extends RecyclerView.Adapter<CollaboratorsAdapter.ViewHolder> implements BitmapDownloader.OnBitmapDownloadedListenner<CollaboratorsAdapter.ViewHolder> {

    private ArrayList<Collaborator> mCollaborators;
    private BitmapDownloader<ViewHolder> mBitmapDownloader;

    public CollaboratorsAdapter(ArrayList<Collaborator> collaborators) {
        mCollaborators = collaborators;

        mBitmapDownloader = new BitmapDownloader<>(new Handler());
        mBitmapDownloader.setOnBitmapDownloadedListenner(this);
        mBitmapDownloader.start();
        mBitmapDownloader.getLooper();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_collaborator, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Collaborator collaborator = mCollaborators.get(position);

        holder.mName.setText(collaborator.mName);

        int width = holder.mPicture.getWidth();
        int height = holder.mPicture.getHeight();

        mBitmapDownloader.queueUrl(holder, collaborator.mPictureUrl, new Size(width, height));
    }

    @Override
    public int getItemCount() {
        return mCollaborators.size();
    }

    @Override
    public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
        holder.mPicture.setImageBitmap(image);
    }

    public static class Collaborator {
        public String mName;
        public String mPictureUrl;
        public String mId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mPicture;
        public TextView mName;
        public ImageButton mRemoveButton;

        public ViewHolder(View view) {
            super(view);

            mPicture = (ImageView) view.findViewById(R.id.collaborator_item_picture);
            mName = (TextView) view.findViewById(R.id.collaborator_item_name);
            mRemoveButton = (ImageButton) view.findViewById(R.id.collaborator_item_remove_bt);

            mRemoveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.equals(mRemoveButton)) {
                removeAt(getAdapterPosition());
            }
        }
    }

    public void removeAt(int position) {
        mCollaborators.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mCollaborators.size());
    }

}
