package com.assignment.sjsu.hudoassenco.cmpe137;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hudoassenco on 11/24/15.
 */
public class CollaboratorsAdapter extends RecyclerView.Adapter<CollaboratorsAdapter.ViewHolder> {

    private ArrayList<Collaborator> _collaborators;

    public CollaboratorsAdapter(ArrayList<Collaborator> _collaborators) {
        this._collaborators = _collaborators;
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
        Collaborator collaborator = _collaborators.get(position);

        holder.name.setText(collaborator.name);
    }

    @Override
    public int getItemCount() {
        return _collaborators.size();
    }

    public static class Collaborator {
        public String name;
        public String pictureUrl;
        public String id;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView picture;
        public TextView name;
        public ImageButton removeButton;

        public ViewHolder(View view) {
            super(view);

            picture = (ImageView) view.findViewById(R.id.collaborator_item_picture);
            name = (TextView) view.findViewById(R.id.collaborator_item_name);
            removeButton = (ImageButton) view.findViewById(R.id.collaborator_item_remove_bt);

            removeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.equals(removeButton)){
                removeAt(getAdapterPosition());
            }
        }
    }

    public void removeAt(int position) {
        _collaborators.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, _collaborators.size());
    }

}
