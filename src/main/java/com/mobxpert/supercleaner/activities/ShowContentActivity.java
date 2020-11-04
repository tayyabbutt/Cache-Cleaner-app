package com.mobxpert.supercleaner.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.ContentAdapter;
import com.mobxpert.supercleaner.databinding.ActivityShowContentBinding;
import com.mobxpert.supercleaner.listeners.OnContentSelectedListener;
import com.mobxpert.supercleaner.managers.ContentManager;
import com.mobxpert.supercleaner.models.GenericFile;
import com.mobxpert.supercleaner.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class ShowContentActivity extends AppCompatActivity implements OnContentSelectedListener {

    private ContentAdapter contentAdapter;
    private ArrayList<GenericFile> contentList;
    private ArrayList<GenericFile> selectedContent;
    private long totalSelectedContentSize = 0;
    private ActivityShowContentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_content);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Soial Media Cleaner");
        }
        this.contentList = ContentManager.getInstance().getContent();
        setupRecyclerView();
        binding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveClicked();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (this.contentList != null) {
            this.contentList.clear();
        }
        ContentManager.getInstance().resetContentList();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void setupRecyclerView() {
        if (this.contentAdapter == null) {
            this.contentAdapter = new ContentAdapter(this, this.contentList, this);
            binding.content.setLayoutManager(new GridLayoutManager(this, 3));
            binding.content.setAdapter(this.contentAdapter);
        }
        this.contentAdapter.notifyDataSetChanged();
    }

    public void onContentSelected(boolean selected, GenericFile genericFile, long fileLength) {
        if (selected) {
            this.totalSelectedContentSize += genericFile.getFile().length();
            if (binding.remove.getVisibility() == View.GONE) {
                binding.remove.setVisibility(View.VISIBLE);
            }
            if (this.selectedContent == null) {
                this.selectedContent = new ArrayList();
            }
            this.selectedContent.add(genericFile);
        } else {
            if (this.totalSelectedContentSize > 0) {
                if (genericFile.getFile().exists()) {
                    this.totalSelectedContentSize -= genericFile.getFile().length();
                } else {
                    this.totalSelectedContentSize -= fileLength;
                }
            }
            if (this.totalSelectedContentSize <= 0 && binding.remove.getVisibility() == View.VISIBLE) {
                binding.remove.setVisibility(View.GONE);
                this.totalSelectedContentSize = 0;
            }
            if (this.selectedContent != null) {
                this.selectedContent.remove(genericFile);
            }
        }
        binding.remove.setText(getString(R.string.remove_content, new Object[]{Utils.bytes2String(this.totalSelectedContentSize)}));
    }

    void onRemoveClicked() {
        if (this.selectedContent == null || this.selectedContent.size() == 0) {
            Toast.makeText(this, "Please select some apk files to REMOVE!", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog alertDialog = new Builder(this).create();
        alertDialog.setTitle(getString(R.string.please_confirm));
        alertDialog.setMessage(getString(R.string.are_you_sure_to_remove));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                for (int i = ShowContentActivity.this.selectedContent.size() - 1; i >= 0; i--) {
                    GenericFile genericFile = (GenericFile) ShowContentActivity.this.selectedContent.get(i);
                    File file = genericFile.getFile();
                    if (file != null && file.exists()) {
                        long fileLength = file.length();
                        if (file.delete()) {
                            ShowContentActivity.this.onContentSelected(false, genericFile, fileLength);
                            ShowContentActivity.this.contentList.remove(genericFile);
                        }
                    }
                }
                if (ShowContentActivity.this.contentAdapter != null) {
                    if (ShowContentActivity.this.contentList.size() == 0) {
                        binding.message.setVisibility(View.VISIBLE);
                    } else {
                       contentAdapter.notifyDataSetChanged();
                    }
                }
   //             AdMobAdsManager.getInstance().showInterstitialAd();
            }
        });
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTransformationMethod(null);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTransformationMethod(null);
    }
}
