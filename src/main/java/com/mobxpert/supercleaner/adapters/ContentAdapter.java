package com.mobxpert.supercleaner.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.listeners.OnContentSelectedListener;
import com.mobxpert.supercleaner.managers.ContentManager;
import com.mobxpert.supercleaner.models.GenericFile;

import java.io.File;
import java.util.ArrayList;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class ContentAdapter extends Adapter<android.support.v7.widget.RecyclerView.ViewHolder> {
    private final String TAG = ContentAdapter.class.getSimpleName();
    private ArrayList<GenericFile> content;
    private ContentManager.ContentType contentType;
    private Context context;
    private OnContentSelectedListener listener;

    class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        ImageView foreground;
        ImageView image;
        TextView name;
        AppCompatCheckBox selected;

        ViewHolder(View itemView) {
            super(itemView);
            foreground = itemView.findViewById(R.id.foreground);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            selected = itemView.findViewById(R.id.selectedq);

        }
    }

    public ContentAdapter(Context context, ArrayList<GenericFile> content, OnContentSelectedListener listener) {
        this.content = content;
        this.context = context;
        this.listener = listener;
        this.contentType = ContentManager.getInstance().getContentType();
    }

    public android.support.v7.widget.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_content, parent, false));
    }

    public void onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {
        GenericFile genericFile = (GenericFile) this.content.get(position);
        if (genericFile != null) {
            File contentFile = genericFile.getFile();
            if (contentFile != null && contentFile.isFile() && contentFile.exists()) {
                final ViewHolder viewHolder = (ViewHolder) holder;
                if (viewHolder != null) {
                    viewHolder.name.setText(genericFile.getFile().getName());
                    int drawableId = -1;
                    if (this.contentType == ContentManager.ContentType.IMAGES) {
                        drawableId = R.drawable.image;
                    } else if (this.contentType == ContentManager.ContentType.AUDIOS) {
                        drawableId = R.drawable.audio;
                    } else if (this.contentType == ContentManager.ContentType.VIDEOS) {
                        drawableId = R.drawable.video;
                    } else if (this.contentType == ContentManager.ContentType.DOCUMENTS) {
                        drawableId = R.drawable.document;
                    } else if (this.contentType == ContentManager.ContentType.OTHERS) {
                        drawableId = R.drawable.others;
                    }
                    if (this.contentType == ContentManager.ContentType.IMAGES || this.contentType == ContentManager.ContentType.VIDEOS) {
                        Glide.with(this.context).load(genericFile.getFile()).apply(new RequestOptions().centerCrop().placeholder(drawableId)).listener(new RequestListener<Drawable>() {
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if (ContentAdapter.this.contentType == ContentManager.ContentType.VIDEOS) {
                                    viewHolder.foreground.setVisibility(View.VISIBLE);
                                }
                                return false;
                            }
                        }).into(viewHolder.image);
                    } else {
                        viewHolder.image.setImageResource(drawableId);
                    }
                    viewHolder.selected.setOnCheckedChangeListener(null);
                    if (genericFile.isSelected()) {
                        viewHolder.selected.setChecked(true);
                    } else {
                        viewHolder.selected.setChecked(false);
                    }
                    viewHolder.selected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            GenericFile genericFile = (GenericFile) ContentAdapter.this.content.get(viewHolder.getAdapterPosition());
                            if (genericFile != null) {
                                genericFile.setSelected(isChecked);
                                if (ContentAdapter.this.listener != null) {
                                    ContentAdapter.this.listener.onContentSelected(isChecked, genericFile, 0);
                                }
                            }
                        }
                    });
                    viewHolder.itemView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ContentAdapter.this.contentClicked(contentType, (GenericFile) ContentAdapter.this.content.get(viewHolder.getAdapterPosition()));
                        }
                    });
                }
            }
        }
    }

    public int getItemCount() {
        return this.content.size();
    }

    public void onViewRecycled(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.selected.setOnCheckedChangeListener(null);
            viewHolder.name.setText(null);
            viewHolder.foreground.setVisibility(View.GONE);
            viewHolder.selected.setChecked(false);
            if (VERSION.SDK_INT >= 16) {
                viewHolder.image.setBackground(null);
            } else {
                viewHolder.image.setBackgroundDrawable(null);
            }
        }
        super.onViewRecycled(holder);
    }

    private void contentClicked(ContentManager.ContentType contentType, GenericFile genericFile) {
        try {
            Intent intent;
            Uri uri;
            String mime;
            MimeTypeMap mimeTypeMap;
            if (contentType == ContentManager.ContentType.AUDIOS) {
                intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                File file = new File(genericFile.getFile().getPath());
                if (file.exists()) {
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    this.context.startActivity(Intent.createChooser(intent, "Complete action using"));
                }
            } else if (contentType == ContentManager.ContentType.VIDEOS) {
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse(genericFile.getFile().getPath()), "video/*");
                this.context.startActivity(Intent.createChooser(intent, "Complete action using"));
            } else if (contentType == ContentManager.ContentType.IMAGES) {
                if (VERSION.SDK_INT < 24) {
                    uri = Uri.fromFile(genericFile.getFile());
                    intent = new Intent("android.intent.action.VIEW");
                    mime = "*/*";
                    mimeTypeMap = MimeTypeMap.getSingleton();
                    if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
                        mime = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    }
                    intent.setDataAndType(uri, mime);
                    this.context.startActivity(intent);
                    return;
                }
                intent = new Intent("android.intent.action.VIEW");
                Uri contentUri = FileProvider.getUriForFile(this.context, this.context.getPackageName() + ".provider.GenericFileProvider", genericFile.getFile());
                this.context.grantUriPermission(this.context.getPackageName(), contentUri, FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                if (VERSION.SDK_INT < 24) {
                    contentUri = Uri.fromFile(genericFile.getFile());
                }
                intent.setData(contentUri);
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                this.context.startActivity(Intent.createChooser(intent, "Complete action using"));
            } else if (contentType == ContentManager.ContentType.DOCUMENTS) {
                uri = Uri.fromFile(genericFile.getFile());
                intent = new Intent("android.intent.action.VIEW");
                mime = "*/*";
                mimeTypeMap = MimeTypeMap.getSingleton();
                if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
                    mime = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                }
                intent.setDataAndType(uri, mime);
                this.context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(this.TAG, "content item clicked: " + e.getLocalizedMessage());
        }
    }
}
