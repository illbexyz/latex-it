package me.albertonicoletti.latex;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the Documents RecyclerView
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 *
 */
public class DocumentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int DRAWER = 0;
    public static final int FILE_CHOOSER = 1;

    /** The object listening to the clicks */
    private DocumentClickListener clickListener;
    /** The dataset */
    private List<File> documentsList;
    /** View type (DRAWER or FILE_CHOOSER) */
    private int viewType;

    /**
     * Constructor
     * @param myDataset The documents list
     * @param clickListener The object listening to the clicks
     * @param viewType Set to DRAWER if the adapter is meant to be used in a drawer,
     *                 Set to FILE_CHOOSER if in the file chooser activity.
     *                 It will change the view type.
     */
    public DocumentsAdapter(List<File> myDataset,
                            DocumentClickListener clickListener,
                            int viewType) {
        this.documentsList = myDataset;
        this.clickListener = clickListener;
        this.viewType = viewType;
    }

    /**
     * Class containing the document's visual representation for the file manager
     */
    public class DocumentViewHolder extends RecyclerView.ViewHolder {

        public TextView documentTitleView;
        public TextView documentLastModifiedView;
        public ImageView fileTypeImage;

        public DocumentViewHolder(View view) {
            super(view);
            documentTitleView = (TextView) view.findViewById(R.id.documentTitle);
            documentLastModifiedView = (TextView) view.findViewById(R.id.last_modified_date);
            fileTypeImage = (ImageView) view.findViewById(R.id.file_type_image);
            view.setOnClickListener(clickListener);
            view.setOnLongClickListener(clickListener);
        }

    }

    /**
     * Class containing the document's visual representation for the drawer list
     */
    public class DrawerDocumentViewHolder extends RecyclerView.ViewHolder {

        public TextView documentTitleView;
        public TextView documentPath;

        public DrawerDocumentViewHolder(View view) {
            super(view);
            documentTitleView = (TextView) view.findViewById(R.id.drawer_document_name);
            documentPath = (TextView) view.findViewById(R.id.drawer_file_path);
            view.setOnClickListener(clickListener);
            view.setOnLongClickListener(clickListener);
        }

    }

    /**
     * Refresh the RecyclerView's view.
     * @param documentsList New document's list
     */
    public void refresh(List<File> documentsList){
        this.documentsList = documentsList;
        this.notifyDataSetChanged();
    }

    /**
     * Performed when creating a new view for an element.
     * @param parent Parent ViewGroup
     * @param viewType ViewType
     * @return ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType == DocumentsAdapter.DRAWER){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_list_item, parent, false);
            viewHolder = new DrawerDocumentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.document_row, parent, false);
            viewHolder = new DocumentViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(viewType == DocumentsAdapter.DRAWER){
            DrawerDocumentViewHolder drawerDocumentViewHolder = (DrawerDocumentViewHolder) holder;
            drawerDocumentViewHolder.documentTitleView.setText(documentsList.get(position).getName());
            drawerDocumentViewHolder.documentPath.setText(documentsList.get(position).getPath());
            if(!documentsList.get(position).exists()){
                SpannableString italicString = new SpannableString(drawerDocumentViewHolder.documentTitleView.getText());
                italicString.setSpan(new StyleSpan(Typeface.ITALIC), 0, italicString.length(), 0);
                drawerDocumentViewHolder.documentTitleView.setText(italicString);
            }
        } else {
            DocumentViewHolder documentViewHolder = (DocumentViewHolder) holder;
            documentViewHolder.documentTitleView.setText(documentsList.get(position).getName());
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
            String lastModified = sdf.format(documentsList.get(position).lastModified());
            documentViewHolder.documentLastModifiedView.setText(lastModified);
            if(documentsList.get(position).isDirectory()){
                documentViewHolder.fileTypeImage.setImageResource(R.drawable.open_document_image);
            } else {
                documentViewHolder.fileTypeImage.setImageResource(R.drawable.document_image);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public int getItemCount() {
        return documentsList.size();
    }

}
