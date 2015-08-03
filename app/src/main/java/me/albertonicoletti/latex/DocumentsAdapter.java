package me.albertonicoletti.latex;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for the Documents RecyclerView
 */
public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> {

    private DocumentClickListener mListener;
    private DocumentClickListener clickListener;

    /** The dataset */
    private List<Document> documentsList;

    /**
     *
     * @param myDataset The documents list
     * @param clickListener
     */
    public DocumentsAdapter(List<Document> myDataset, DocumentClickListener clickListener) {
        this.documentsList = myDataset;
        this.clickListener = clickListener;
    }

    /**
     * Class containing the document's visual representation
     */
    public class DocumentViewHolder extends RecyclerView.ViewHolder{

        public TextView documentTitleView;
        public TextView documentLastModifiedView;

        public DocumentViewHolder(View view) {
            super(view);
            documentTitleView = (TextView) view.findViewById(R.id.documentTitle);
            documentLastModifiedView = (TextView) view.findViewById(R.id.last_modified_date);
            view.setOnClickListener(clickListener);
            view.setOnLongClickListener(clickListener);
        }

    }

    /**
     * Refresh the RecyclerView's view.
     * @param documentsList New document's list
     */
    public void refresh(List<Document> documentsList){
        this.documentsList = documentsList;
        this.notifyDataSetChanged();
    }

    /**
     * Performed when creating a new view for an element.
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_row, parent, false);

        DocumentViewHolder documentViewHolder = new DocumentViewHolder(view);
        return documentViewHolder;
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.documentTitleView.setText(documentsList.get(position).getTitle());
        holder.documentLastModifiedView.setText(documentsList.get(position).getLastModified());
    }

    @Override
    public int getItemCount() {
        return documentsList.size();
    }

}
