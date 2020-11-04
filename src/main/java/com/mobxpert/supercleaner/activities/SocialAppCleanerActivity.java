package com.mobxpert.supercleaner.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.databinding.ActivitySocialAppCleanerBinding;
import com.mobxpert.supercleaner.managers.ActivityManager;
import com.mobxpert.supercleaner.managers.ContentManager;
import com.mobxpert.supercleaner.managers.ProgressDialogManager;
import com.mobxpert.supercleaner.models.GenericFile;
import com.mobxpert.supercleaner.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class SocialAppCleanerActivity extends AppCompatActivity {
    private final String TAG = SocialAppCleanerActivity.class.getSimpleName();
    private ArrayMap<String, ArrayList<GenericFile>> content;
    private Thread getAllContentThread;
    private boolean isActivityDestroyed = false;
    private SocialApp socialApp = SocialApp.NOTHING;
    private ActivitySocialAppCleanerBinding binding;

    private enum SocialApp {
        NOTHING,
        WHATSAPP,
        FACEBOOK,
        MESSENGER,
        INSTAGRAM,
        SNAPCHAT,
        TWITTER
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_social_app_cleaner);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Soial Media Cleaner");
        }

        // AdMobAdsManager.getInstance().loadBanner(binding.adContainer);
        AdView adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.RECTANGLE_HEIGHT_250);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();

        binding.clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCleanClicked();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            String app = getIntent().getStringExtra(getString(R.string.social_app));
            if (app != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.social_cleaner, new Object[]{app}));
                }
                if (app.equals(getString(R.string.whatsapp))) {
                    getWhatsAppContent();
                    this.socialApp = SocialApp.WHATSAPP;
                } else if (app.equals(getString(R.string.facebook))) {
                    getFacebookContent();
                    this.socialApp = SocialApp.FACEBOOK;
                } else if (app.equals(getString(R.string.messenger))) {
                    getMITContent();
                    this.socialApp = SocialApp.MESSENGER;
                } else if (app.equals(getString(R.string.instagram))) {
                    getMITContent();
                    this.socialApp = SocialApp.INSTAGRAM;
                } else if (app.equals(getString(R.string.snapchat))) {
                    getSnapchatContent();
                    this.socialApp = SocialApp.SNAPCHAT;
                } else if (app.equals(getString(R.string.twitter))) {
                    getMITContent();
                    this.socialApp = SocialApp.TWITTER;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.isActivityDestroyed = true;
        if (this.getAllContentThread != null && this.getAllContentThread.isAlive()) {
            this.getAllContentThread.interrupt();
        }
        if (this.content != null) {
            this.content.clear();
        }
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

    public String[] getExternalStorageDirectories() {
        int i;
        List<String> results = new ArrayList();
        if (VERSION.SDK_INT >= 19) {
            File[] externalDirs = getExternalFilesDirs(null);
            if (externalDirs != null && externalDirs.length > 0) {
                for (File file : externalDirs) {
                    if (file != null) {
                        String[] paths = file.getPath().split("/Android");
                        if (paths != null && paths.length > 0) {
                            boolean addPath;
                            String path = paths[0];
                            if (VERSION.SDK_INT >= 21) {
                                addPath = Environment.isExternalStorageRemovable(file);
                            } else {
                                addPath = "mounted".equals(EnvironmentCompat.getStorageState(file));
                            }
                            if (addPath) {
                                results.add(path);
                            }
                        }
                    }
                }
            }
        }
        if (results.isEmpty()) {
            String output = "";
            InputStream is = null;
            try {
                Process process = new ProcessBuilder(new String[0]).command(new String[]{"mount | grep /dev/block/vold"}).redirectErrorStream(true).start();
                process.waitFor();
                is = process.getInputStream();
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (Exception e) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        Log.e(this.TAG, "inner catch (closing input stream) exception: " + e.getLocalizedMessage());
                    }
                }
                Log.e(this.TAG, "outer catch exception: " + e.getLocalizedMessage());
            }
            if (!output.trim().isEmpty()) {
                String[] devicePoints = output.split("\n");
                if (devicePoints.length > 0) {
                    for (String voldPoint : devicePoints) {
                        results.add(voldPoint.split(" ")[2]);
                    }
                }
            }
        }
        int i2;
        if (VERSION.SDK_INT >= 23) {
            i = 0;
            while (i < results.size()) {
                if (!((String) results.get(i)).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(this.TAG, ((String) results.get(i)) + " might not be extSDcard");
                    i2 = i - 1;
                    results.remove(i);
                    i = i2;
                }
                i++;
            }
        } else {
            i = 0;
            while (i < results.size()) {
                if (!(((String) results.get(i)).toLowerCase().contains("ext") || ((String) results.get(i)).toLowerCase().contains("sdcard"))) {
                    Log.d(this.TAG, ((String) results.get(i)) + " might not be extSDcard");
                    i2 = i - 1;
                    results.remove(i);
                    i = i2;
                }
                i++;
            }
        }
        String[] storageDirectories = new String[results.size()];
        for (i = 0; i < results.size(); i++) {
            storageDirectories[i] = (String) results.get(i);
        }
        return storageDirectories;
    }

    private void getAllContent(File directory) {
        if (directory == null) {
            return;
        }
        if (directory.isDirectory()) {
            File[] subFiles = directory.listFiles();
            if (subFiles != null && subFiles.length > 0) {
                for (File file : subFiles) {
                    if (file != null) {
                        if (file.isDirectory()) {
                            getAllContent(file);
                        } else {
                            detectFileTypeAndAddInCategory(file);
                        }
                    }
                }
            }
        } else if (directory.isFile()) {
            detectFileTypeAndAddInCategory(directory);
        }
    }

    private void detectFileTypeAndAddInCategory(File file) {
        String fileName = file.getName();
        ArrayList<GenericFile> images;
        GenericFile genericFile;
        ArrayList<GenericFile> videos;
        if (this.socialApp == SocialApp.WHATSAPP) {
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
                images = (ArrayList) this.content.get(getString(R.string.images));
                if (images != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    images.add(genericFile);
                }
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".aac") || fileName.endsWith(".amr") || fileName.endsWith(".m4a") || fileName.endsWith(".ogg") || fileName.endsWith(".wav") || fileName.endsWith(".flac") || fileName.endsWith(".opus")) {
                ArrayList<GenericFile> audios = (ArrayList) this.content.get(getString(R.string.audios));
                if (audios != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    audios.add(genericFile);
                }
            } else if (fileName.endsWith(".3gp") || fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".webm")) {
                videos = (ArrayList) this.content.get(getString(R.string.videos));
                if (videos != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    videos.add(genericFile);
                }
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".html") || fileName.endsWith(".pdf") || fileName.endsWith(".txt") || fileName.endsWith(".xml") || fileName.endsWith(".xlsx")) {
                ArrayList<GenericFile> documents = (ArrayList) this.content.get(getString(R.string.documents));
                if (documents != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    documents.add(genericFile);
                }
            } else if (fileName.endsWith(".apk") || fileName.endsWith(".zip") || fileName.endsWith(".vcf")) {
                ArrayList<GenericFile> others = (ArrayList) this.content.get(getString(R.string.others));
                if (others != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    others.add(genericFile);
                }
            }
        } else if (this.socialApp == SocialApp.FACEBOOK) {
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
                images = (ArrayList) this.content.get(getString(R.string.images));
                if (images != null) {
                    genericFile = new GenericFile();
                    genericFile.setFile(file);
                    images.add(genericFile);
                }
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
            images = (ArrayList) this.content.get(getString(R.string.images));
            if (images != null) {
                genericFile = new GenericFile();
                genericFile.setFile(file);
                images.add(genericFile);
            }
        } else if (fileName.endsWith(".3gp") || fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".webm")) {
            videos = (ArrayList) this.content.get(getString(R.string.videos));
            if (videos != null) {
                genericFile = new GenericFile();
                genericFile.setFile(file);
                videos.add(genericFile);
            }
        }
    }

    private void getWhatsAppContent() {
        this.getAllContentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SocialAppCleanerActivity.this.content == null) {
                    SocialAppCleanerActivity.this.content = new ArrayMap();
                }
                SocialAppCleanerActivity.this.content.clear();
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.images), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.audios), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.videos), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.documents), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.others), new ArrayList());
                Log.d(SocialAppCleanerActivity.this.TAG, "whatsapp content collection started");
                File whatsAppDirectory = new File(Environment.getExternalStorageDirectory(), SocialAppCleanerActivity.this.getString(R.string.whatsapp));
                if (whatsAppDirectory.exists()) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "external storage directory: " + whatsAppDirectory);
                    SocialAppCleanerActivity.this.getAllContent(whatsAppDirectory);
                }
                String[] externalStoragePaths = SocialAppCleanerActivity.this.getExternalStorageDirectories();
                if (externalStoragePaths != null && externalStoragePaths.length > 0) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total Paths are: " + externalStoragePaths.length);
                    for (String path : externalStoragePaths) {
                        File file = new File(path);
                        if (file.exists()) {
                            File[] subFiles = file.listFiles();
                            if (subFiles != null && subFiles.length > 0) {
                                for (File subFile : subFiles) {
                                    if (subFile != null) {
                                        SocialAppCleanerActivity.this.getAllContent(subFile);
                                    }
                                }
                            } else if (file.isFile()) {
                                SocialAppCleanerActivity.this.getAllContent(file);
                            }
                        }
                    }
                }
                SocialAppCleanerActivity.this.sortContentInDescending();
                if (SocialAppCleanerActivity.this.content != null) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total other files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.others))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total audio files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.audios))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total video files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.videos))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total image files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.images))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total document files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.documents))).size());
                }
                SocialAppCleanerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SocialAppCleanerActivity.this.TAG, "whatsapp content collection finished");
                        SocialAppCleanerActivity.this.setContentContainer();
                    }
                });
            }
        });
        this.getAllContentThread.start();
    }

    private void getFacebookContent() {
        this.getAllContentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SocialAppCleanerActivity.this.content == null) {
                    SocialAppCleanerActivity.this.content = new ArrayMap();
                }
                SocialAppCleanerActivity.this.content.clear();
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.images), new ArrayList());
                Log.d(SocialAppCleanerActivity.this.TAG, "facebook content collection started");
                File rootDirectory = new File(Environment.getExternalStorageDirectory(), SocialAppCleanerActivity.this.getString(R.string.dcim));
                if (rootDirectory.exists()) {
                    File facebookDirectory = new File(rootDirectory, SocialAppCleanerActivity.this.getString(R.string.facebook));
                    if (facebookDirectory.exists()) {
                        Log.d(SocialAppCleanerActivity.this.TAG, "external storage directory: " + facebookDirectory);
                        SocialAppCleanerActivity.this.getAllContent(facebookDirectory);
                    }
                }
                String[] externalStoragePaths = SocialAppCleanerActivity.this.getExternalStorageDirectories();
                if (externalStoragePaths != null && externalStoragePaths.length > 0) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total Paths are: " + externalStoragePaths.length);
                    for (String path : externalStoragePaths) {
                        File file = new File(path);
                        if (file.exists()) {
                            File[] subFiles = file.listFiles();
                            if (subFiles != null && subFiles.length > 0) {
                                for (File subFile : subFiles) {
                                    if (subFile != null) {
                                        SocialAppCleanerActivity.this.getAllContent(subFile);
                                    }
                                }
                            } else if (file.isFile()) {
                                SocialAppCleanerActivity.this.getAllContent(file);
                            }
                        }
                    }
                }
                SocialAppCleanerActivity.this.sortContentInDescending();
                if (SocialAppCleanerActivity.this.content != null) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total image files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.images))).size());
                }
                SocialAppCleanerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SocialAppCleanerActivity.this.TAG, "facebook content collection finished");
                        SocialAppCleanerActivity.this.setContentContainer();
                    }
                });
            }
        });
        this.getAllContentThread.start();
    }

    private void getMITContent() {
        this.getAllContentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File messengerDirectory;
                if (SocialAppCleanerActivity.this.content == null) {
                    SocialAppCleanerActivity.this.content = new ArrayMap();
                }
                SocialAppCleanerActivity.this.content.clear();
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.images), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.videos), new ArrayList());
                String app = null;
                if (SocialAppCleanerActivity.this.socialApp == SocialApp.MESSENGER) {
                    app = SocialAppCleanerActivity.this.getString(R.string.messenger);
                } else if (SocialAppCleanerActivity.this.socialApp == SocialApp.INSTAGRAM) {
                    app = SocialAppCleanerActivity.this.getString(R.string.instagram);
                } else if (SocialAppCleanerActivity.this.socialApp == SocialApp.TWITTER) {
                    app = SocialAppCleanerActivity.this.getString(R.string.twitter);
                }
                Log.d(SocialAppCleanerActivity.this.TAG, app + " content collection started");
                File picturesRootDirectory = new File(Environment.getExternalStorageDirectory(), SocialAppCleanerActivity.this.getString(R.string.pictures));
                if (picturesRootDirectory.exists()) {
                    messengerDirectory = new File(picturesRootDirectory, app);
                    if (messengerDirectory.exists()) {
                        Log.d(SocialAppCleanerActivity.this.TAG, "external storage directory: " + messengerDirectory);
                        SocialAppCleanerActivity.this.getAllContent(messengerDirectory);
                    }
                }
                File moviesRootDirectory = new File(Environment.getExternalStorageDirectory(), SocialAppCleanerActivity.this.getString(R.string.movies));
                if (moviesRootDirectory.exists()) {
                    messengerDirectory = new File(moviesRootDirectory, app);
                    if (messengerDirectory.exists()) {
                        Log.d(SocialAppCleanerActivity.this.TAG, "external storage directory: " + messengerDirectory);
                        SocialAppCleanerActivity.this.getAllContent(messengerDirectory);
                    }
                }
                String[] externalStoragePaths = SocialAppCleanerActivity.this.getExternalStorageDirectories();
                if (externalStoragePaths != null && externalStoragePaths.length > 0) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total Paths are: " + externalStoragePaths.length);
                    for (String path : externalStoragePaths) {
                        File file = new File(path);
                        if (file.exists()) {
                            File[] subFiles = file.listFiles();
                            if (subFiles != null && subFiles.length > 0) {
                                for (File subFile : subFiles) {
                                    if (subFile != null) {
                                        SocialAppCleanerActivity.this.getAllContent(subFile);
                                    }
                                }
                            } else if (file.isFile()) {
                                SocialAppCleanerActivity.this.getAllContent(file);
                            }
                        }
                    }
                }
                SocialAppCleanerActivity.this.sortContentInDescending();
                if (SocialAppCleanerActivity.this.content != null) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total video files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.videos))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total image files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.images))).size());
                }
                final String finalApp = app;
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(SocialAppCleanerActivity.this.TAG, finalApp + " content collection finished");
                        SocialAppCleanerActivity.this.setContentContainer();
                    }
                });
            }
        });
        this.getAllContentThread.start();
    }

    private void getSnapchatContent() {
        this.getAllContentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SocialAppCleanerActivity.this.content == null) {
                    SocialAppCleanerActivity.this.content = new ArrayMap();
                }
                SocialAppCleanerActivity.this.content.clear();
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.videos), new ArrayList());
                SocialAppCleanerActivity.this.content.put(SocialAppCleanerActivity.this.getString(R.string.images), new ArrayList());
                Log.d(SocialAppCleanerActivity.this.TAG, "snapchat content collection started");
                File rootDirectory = new File(Environment.getExternalStorageDirectory(), SocialAppCleanerActivity.this.getString(R.string.snapchat));
                if (rootDirectory.exists()) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "external storage directory: " + rootDirectory);
                    SocialAppCleanerActivity.this.getAllContent(rootDirectory);
                }
                String[] externalStoragePaths = SocialAppCleanerActivity.this.getExternalStorageDirectories();
                if (externalStoragePaths != null && externalStoragePaths.length > 0) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total Paths are: " + externalStoragePaths.length);
                    for (String path : externalStoragePaths) {
                        File file = new File(path);
                        if (file.exists()) {
                            File[] subFiles = file.listFiles();
                            if (subFiles != null && subFiles.length > 0) {
                                for (File subFile : subFiles) {
                                    if (subFile != null) {
                                        SocialAppCleanerActivity.this.getAllContent(subFile);
                                    }
                                }
                            } else if (file.isFile()) {
                                SocialAppCleanerActivity.this.getAllContent(file);
                            }
                        }
                    }
                }
                SocialAppCleanerActivity.this.sortContentInDescending();
                if (SocialAppCleanerActivity.this.content != null) {
                    Log.d(SocialAppCleanerActivity.this.TAG, "total video files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.videos))).size());
                    Log.d(SocialAppCleanerActivity.this.TAG, "total image files: " + ((ArrayList) SocialAppCleanerActivity.this.content.get(SocialAppCleanerActivity.this.getString(R.string.images))).size());
                }
                SocialAppCleanerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SocialAppCleanerActivity.this.TAG, "snapchat content collection finished");
                        SocialAppCleanerActivity.this.setContentContainer();
                    }
                });
            }
        });
        this.getAllContentThread.start();
    }

    private void sortContentInDescending() {
        if (this.content != null) {
            for (String key : this.content.keySet()) {
                ArrayList<GenericFile> genericFiles = (ArrayList) this.content.get(key);
                if (genericFiles != null && genericFiles.size() > 0) {
                    Collections.sort(genericFiles, new Comparator<GenericFile>() {
                        @Override
                        public int compare(GenericFile leftFile, GenericFile rightFile) {
                            return new Date(rightFile.getFile().lastModified()).compareTo(new Date(leftFile.getFile().lastModified()));
                        }
                    });
                }
            }
        }
    }

    private void setSpaceStats() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (SocialAppCleanerActivity.this.content != null) {
                    long totalSize = 0;
                    for (String key : SocialAppCleanerActivity.this.content.keySet()) {
                        ArrayList<GenericFile> files = (ArrayList) SocialAppCleanerActivity.this.content.get(key);
                        if (files != null && files.size() > 0) {
                            Iterator it = files.iterator();
                            while (it.hasNext()) {
                                File file = ((GenericFile) it.next()).getFile();
                                if (file != null && file.isFile() && file.exists()) {
                                    totalSize += file.length();
                                }
                            }
                        }
                    }
                    final long totalFinalSize = totalSize;
                    SocialAppCleanerActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (!SocialAppCleanerActivity.this.isActivityDestroyed && totalFinalSize > 0) {
                                binding.appbar.setVisibility(View.VISIBLE);
                                bytes2String(totalFinalSize);
//                                String space = Utils.bytes2String(totalFinalSize);
//                                if (space.contains(" ")) {
//                                    String[] spaceParts = space.split(" ");
//                                    if (spaceParts.length == 2) {
//                                        binding.total.setText(spaceParts[0]);
//                                        binding.unit.setText(spaceParts[1]);
//                                    }
//                                }
                            }
                        }
                    });
                }
            }
        }).start();
    }


    private void bytes2String(long length) {
        NumberFormat numberFormat = new DecimalFormat();
        numberFormat.setMaximumFractionDigits(1);
        if (((double) length) < 1024.0d) {

            try {
                binding.total.setText(numberFormat.format(length));
                binding.unit.setText("Byte(s)");
            } catch (Exception e) {
                binding.total.setText(length + "");
                binding.unit.setText("Byte(s)");
            }
        } else if (((double) length) < 1048576.0d) {
            binding.total.setText(numberFormat.format(((double) length) / 1024.0d));
            binding.unit.setText("KB");
        } else {
            if (((double) length) < 1.073741824E9d) {
                binding.total.setText(numberFormat.format(((double) length) / 1048576.0d));
                binding.unit.setText("MB");
            } else if (((double) length) < 1.099511627776E12d) {
                binding.total.setText(numberFormat.format(((double) length) / 1.073741824E9d));
                binding.unit.setText("GB");
            } else {
                binding.total.setText(numberFormat.format(((double) length) / 1.099511627776E12d));
                binding.unit.setText("TB");
            }
        }
    }

    void onCleanClicked() {
        final AlertDialog alertDialog = new Builder(this).create();
        alertDialog.setTitle(getString(R.string.please_confirm));
        alertDialog.setMessage(getString(R.string.are_you_sure_to_clean));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (SocialAppCleanerActivity.this.content != null) {
                    ProgressDialogManager.getInstance().showProgressDialog(SocialAppCleanerActivity.this, SocialAppCleanerActivity.this.getString(R.string.removing_content), false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (String key : SocialAppCleanerActivity.this.content.keySet()) {
                                ArrayList<GenericFile> files = (ArrayList) SocialAppCleanerActivity.this.content.get(key);
                                if (files != null && files.size() > 0) {
                                    Iterator it = files.iterator();
                                    while (it.hasNext()) {
                                        File file = ((GenericFile) it.next()).getFile();
                                        if (file != null && file.isFile() && file.exists()) {
                                            file.delete();
                                        }
                                    }
                                }
                            }
                            SocialAppCleanerActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!SocialAppCleanerActivity.this.isActivityDestroyed) {
                                        SocialAppCleanerActivity.this.getWhatsAppContent();
                                        ProgressDialogManager.getInstance().hideProgressDialog();
   //                                     AdMobAdsManager.getInstance().showInterstitialAd();
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTransformationMethod(null);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTransformationMethod(null);
    }

    private void setContentContainer() {
        setSpaceStats();
        if (binding.contentContainer != null && binding.contentContainer.getChildCount() > 0) {
            binding.contentContainer.removeAllViews();
        }
        if (this.content != null) {
            for (int i = 0; i < this.content.keySet().size(); i++) {
                int index = -1;
                if (i == 0) {
                    if (this.content.containsKey(getString(R.string.images))) {
                        index = this.content.indexOfKey(getString(R.string.images));
                    }
                } else if (i == 1) {
                    if (this.content.containsKey(getString(R.string.videos))) {
                        index = this.content.indexOfKey(getString(R.string.videos));
                    }
                } else if (i == 2) {
                    if (this.content.containsKey(getString(R.string.audios))) {
                        index = this.content.indexOfKey(getString(R.string.audios));
                    }
                } else if (i == 3) {
                    if (this.content.containsKey(getString(R.string.documents))) {
                        index = this.content.indexOfKey(getString(R.string.documents));
                    }
                } else if (i == 4 && this.content.containsKey(getString(R.string.others))) {
                    index = this.content.indexOfKey(getString(R.string.others));
                }
                if (index != -1) {
                    long length = 0;
                    String key = (String) this.content.keyAt(index);
                    View contentView = LayoutInflater.from(this).inflate(R.layout.layout_content_type, null, false);
                    TextView title = (TextView) contentView.findViewById(R.id.title);
                    TextView count = (TextView) contentView.findViewById(R.id.count);
                    TextView size = (TextView) contentView.findViewById(R.id.size);
                    RelativeLayout layout1 = (RelativeLayout) contentView.findViewById(R.id.layout1);
                    RelativeLayout layout2 = (RelativeLayout) contentView.findViewById(R.id.layout2);
                    RelativeLayout layout3 = (RelativeLayout) contentView.findViewById(R.id.layout3);
                    ImageView foreground1 = (ImageView) contentView.findViewById(R.id.foreground1);
                    ImageView foreground2 = (ImageView) contentView.findViewById(R.id.foreground2);
                    ImageView foreground3 = (ImageView) contentView.findViewById(R.id.foreground3);
                    ImageView image1 = (ImageView) contentView.findViewById(R.id.image1);
                    ImageView image2 = (ImageView) contentView.findViewById(R.id.image2);
                    ImageView image3 = (ImageView) contentView.findViewById(R.id.image3);
                    Button more = (Button) contentView.findViewById(R.id.more);
                    ArrayList<GenericFile> genericFiles = (ArrayList) this.content.get(key);
                    if (genericFiles.size() < 3) {
                        if (genericFiles.size() == 0 || genericFiles.size() == 1) {
                            layout2.setVisibility(View.GONE);
                            layout3.setVisibility(View.GONE);
                        } else if (genericFiles.size() == 2) {
                            layout3.setVisibility(View.GONE);
                        }
                    }
                    if (title != null) {
                        title.setText(key);
                    }
                    int contentDrawableId = -1;
                    int contentTypeDrawableId = -1;
                    if (!key.equals(getString(R.string.images))) {
                        if (!key.equals(getString(R.string.audios))) {
                            if (!key.equals(getString(R.string.videos))) {
                                if (!key.equals(getString(R.string.documents))) {
                                    if (key.equals(getString(R.string.others)) && count != null) {
                                        contentDrawableId = R.drawable.others;
                                        contentTypeDrawableId = R.drawable.ic_other;
                                        count.setText(getString(R.string.n_others, new Object[]{Integer.valueOf(genericFiles.size())}));
                                    }
                                } else if (count != null) {
                                    contentDrawableId = R.drawable.document;
                                    contentTypeDrawableId = R.drawable.ic_document;
                                    count.setText(getString(R.string.n_documents, new Object[]{Integer.valueOf(genericFiles.size())}));
                                }
                            } else if (count != null) {
                                contentDrawableId = R.drawable.video;
                                contentTypeDrawableId = R.drawable.ic_video;
                                count.setText(getString(R.string.n_videos, new Object[]{Integer.valueOf(genericFiles.size())}));
                            }
                        } else if (count != null) {
                            contentDrawableId = R.drawable.audio;
                            contentTypeDrawableId = R.drawable.ic_audio;
                            count.setText(getString(R.string.n_audios, new Object[]{Integer.valueOf(genericFiles.size())}));
                        }
                    } else if (count != null) {
                        contentDrawableId = R.drawable.image;
                        contentTypeDrawableId = R.drawable.ic_image;
                        count.setText(getString(R.string.n_images, new Object[]{Integer.valueOf(genericFiles.size())}));
                    }
                    if (!(contentDrawableId == -1 || title == null)) {
                        title.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, contentTypeDrawableId), null, null, null);
                    }
                    image1.setImageResource(contentDrawableId);
                    image2.setImageResource(contentDrawableId);
                    image3.setImageResource(contentDrawableId);
                    if (genericFiles.size() > 0) {
                        Iterator it = genericFiles.iterator();
                        while (it.hasNext()) {
                            GenericFile genericFile = (GenericFile) it.next();
                            if (genericFile != null) {
                                File file = genericFile.getFile();
                                if (file != null && file.exists() && file.isFile() && file.length() > 0) {
                                    length += genericFile.getFile().length();
                                }
                            }
                        }
                    }
                    String totalSize = Utils.bytes2String(length);
                    if (size != null) {
                        size.setText(totalSize);
                    }
                    if (!key.equals(getString(R.string.images))) {
                    }
                    if (genericFiles.size() > 0) {
                        for (int j = 0; j < genericFiles.size(); j++) {
                            ImageView target = null;
                            if (j == 0) {
                                target = image1;
                            }
                            if (j == 1) {
                                target = image2;
                            }
                            if (j == 2) {
                                target = image3;
                            }
                            if (target != null) {
                                try {
                                    Glide.with((FragmentActivity) this).load(((GenericFile) genericFiles.get(j)).getFile()).apply(new RequestOptions().centerCrop()).listener(new RequestListener<Drawable>() {
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            return false;
                                        }
                                    }).into(target);
                                    if (key.equals(getString(R.string.videos))) {
                                        if (target == image1) {
                                            foreground1.setVisibility(View.VISIBLE);
                                        } else if (target == image2) {
                                            foreground2.setVisibility(View.VISIBLE);
                                        } else if (target == image3) {
                                            foreground3.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(this.TAG, "Error loading image using Glide from path: " + ((GenericFile) genericFiles.get(j)).getFile().getPath() + " " + e.getLocalizedMessage());
                                }
                            }
                        }
                    }
                    final ArrayList<GenericFile> arrayList = new ArrayList<>();
                    if (genericFiles != null) {
                        arrayList.addAll(genericFiles);
                    }
                    final String str = key;
                    layout1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (arrayList != null && arrayList.size() > 0) {
                                SocialAppCleanerActivity.this.contentClicked(str, (GenericFile) arrayList.get(0));
                            }
                        }
                    });
                    layout2.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (arrayList != null && arrayList.size() > 1) {
                                SocialAppCleanerActivity.this.contentClicked(str, (GenericFile) arrayList.get(1));
                            }
                        }
                    });
                    layout3.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (arrayList != null && arrayList.size() > 2) {
                                SocialAppCleanerActivity.this.contentClicked(str, (GenericFile) arrayList.get(2));
                            }
                        }
                    });
                    more.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (arrayList == null || arrayList.size() == 0) {
                                Toast.makeText(SocialAppCleanerActivity.this, SocialAppCleanerActivity.this.getString(R.string.content_not_found), Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (str.equals(SocialAppCleanerActivity.this.getString(R.string.images))) {
                                ContentManager.getInstance().setContentType(ContentManager.ContentType.IMAGES);
                            } else if (str.equals(SocialAppCleanerActivity.this.getString(R.string.audios))) {
                                ContentManager.getInstance().setContentType(ContentManager.ContentType.AUDIOS);
                            } else if (str.equals(SocialAppCleanerActivity.this.getString(R.string.videos))) {
                                ContentManager.getInstance().setContentType(ContentManager.ContentType.VIDEOS);
                            } else if (str.equals(SocialAppCleanerActivity.this.getString(R.string.documents))) {
                                ContentManager.getInstance().setContentType(ContentManager.ContentType.DOCUMENTS);
                            } else if (str.equals(SocialAppCleanerActivity.this.getString(R.string.others))) {
                                ContentManager.getInstance().setContentType(ContentManager.ContentType.OTHERS);
                            }
                            ContentManager.getInstance().setGenericFiles(arrayList);
                            ActivityManager.getInstance().openNewActivity(SocialAppCleanerActivity.this, ShowContentActivity.class, true);
                        }
                    });
                    binding.contentContainer.addView(contentView);
                }
            }
        }
    }

    private void contentClicked(String key, GenericFile genericFile) {
        try {
            Intent intent;
            Uri uri;
            String mime;
            MimeTypeMap mimeTypeMap;
            if (key.equals(getString(R.string.audios))) {
                intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                File file = new File(genericFile.getFile().getPath());
                if (file.exists()) {
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(Intent.createChooser(intent, "Complete action using"));
                }
            } else if (key.equals(getString(R.string.videos))) {
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse(genericFile.getFile().getPath()), "video/*");
                startActivity(Intent.createChooser(intent, "Complete action using"));
            } else if (key.equals(getString(R.string.images))) {
                if (VERSION.SDK_INT < 24) {
                    uri = Uri.fromFile(genericFile.getFile());
                    intent = new Intent("android.intent.action.VIEW");
                    mime = "*/*";
                    mimeTypeMap = MimeTypeMap.getSingleton();
                    if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
                        mime = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    }
                    intent.setDataAndType(uri, mime);
                    startActivity(intent);
                    return;
                }
                intent = new Intent("android.intent.action.VIEW");
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider.GenericFileProvider", genericFile.getFile());
                grantUriPermission(getPackageName(), contentUri, FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                if (VERSION.SDK_INT < 24) {
                    contentUri = Uri.fromFile(genericFile.getFile());
                }
                intent.setData(contentUri);
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Complete action using"));
            } else if (key.equals(getString(R.string.documents))) {
                uri = Uri.fromFile(genericFile.getFile());
                intent = new Intent("android.intent.action.VIEW");
                mime = "*/*";
                mimeTypeMap = MimeTypeMap.getSingleton();
                if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
                    mime = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                }
                intent.setDataAndType(uri, mime);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(this.TAG, "content item clicked: " + e.getLocalizedMessage());
        }
    }
}
