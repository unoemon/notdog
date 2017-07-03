package com.unoemon.notdog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rekognition.model.Label;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.roger.catloadinglibrary.CatLoadingView;
import com.unoemon.notdog.log.Logger;
import com.unoemon.notdog.util.ApiUtil;
import com.unoemon.notdog.util.AppConst;
import com.unoemon.notdog.util.BitmapUtil;
import com.unoemon.notdog.util.DisposableManager;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.unoemon.notdog.util.AppConst.MAXIMUM_SIZE;
import static com.unoemon.notdog.util.AwsConst.IDENTITY_POOL_ID;

/**
 * Main actibity
 * Amazon Rekognition Samples for Android
 */
public class MainActivity extends AppCompatActivity {

    private static final String KEY_LABELS = "key_labels";
    private static final String KEY_URI_STRING = "key_uri_string";

    private String uriString;
    private List<Label> labels;

    @BindView(R.id.imageview_contents)
    ImageView contentsImage;

    @BindView(R.id.textview_answer)
    TextView answerText;

    @BindView(R.id.textview_lists)
    TextView listsText;

    CatLoadingView loadingView;

    @OnClick(R.id.button_camera)
    void button_camera() {
        getDetectLabels(Sources.CAMERA);
    }

    @OnClick(R.id.button_share)
    void button_share() {
        doShare();

    }

    @OnClick(R.id.button_gallery)
    void button_gallery() {
        getDetectLabels(Sources.GALLERY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(uriString)) {
            outState.putString(KEY_URI_STRING, uriString);
        }
        if (labels != null) {
            outState.putSerializable(KEY_LABELS, (Serializable) labels);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadingView = new CatLoadingView();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_URI_STRING)) {
                uriString = savedInstanceState.getString(KEY_URI_STRING);
                if (uriString != null) {
                    Uri uri = Uri.parse(uriString);
                    Disposable dispose = RxImageConverters.uriToBitmap(MainActivity.this, uri)
                            .onErrorResumeNext(Observable.empty())
                            .subscribe(this::updateBackground);
                    DisposableManager.add(dispose);
                }
            }
            if (savedInstanceState.containsKey(KEY_LABELS)) {
                labels = (List<Label>) savedInstanceState.getSerializable(KEY_LABELS);
                updateLabels(labels);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Logger.d("");
        DisposableManager.dispose();
        super.onDestroy();
    }


    private void getDetectLabels(Sources sources) {
        Logger.d("");


        if(IDENTITY_POOL_ID.length() == 0){
            Toast.makeText(getApplicationContext(), "Please set YOUR_IDENTITY_POOL_ID", Toast.LENGTH_LONG).show();
            return;
        }

        //init
        answerText.setText("");
        answerText.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        listsText.setText("");

        Disposable disposable = RxImagePicker.with(MainActivity.this).requestImage(sources)
                .doOnNext(uri -> {
                    uriString = uri.toString();
                    loadingView.show(getSupportFragmentManager(), "");
                })
                .subscribeOn(Schedulers.io())
                .flatMap(uri -> RxImageConverters.uriToBitmap(MainActivity.this, uri))
                .doOnNext(this::updateBackground)
                .flatMap(orgBitmap -> Observable.just(orgBitmap)
                        .subscribeOn(Schedulers.io())
                        .flatMap(bitmap -> ApiUtil.getDetectLabels(BitmapUtil.resize(bitmap, MAXIMUM_SIZE, MAXIMUM_SIZE))))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> loadingView.dismiss())
                .subscribe(labels -> {
                    Logger.d("onNext!");
                    updateLabels(labels);
                }, e -> {
                    Logger.d("onError! %s", e);
                    String errorMessage = e.getMessage();
                    if (e instanceof AmazonServiceException) {
                        errorMessage = ((AmazonServiceException) e).getErrorMessage();
                    }
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                });

        DisposableManager.add(disposable);

    }

    private void doFoundDog(String confidence) {
        Logger.d("");
        answerText.setText("HIT DOG ! " + confidence + "%");
        answerText.setBackgroundColor(ContextCompat.getColor(this, R.color.green));

    }

    private void doFoundHotdog(String confidence) {
        Logger.d("");
        answerText.setText("HOT DOG ! " + confidence + "%");
        answerText.setBackgroundColor(ContextCompat.getColor(this, R.color.mustard));

    }

    private void doNotDog() {
        Logger.d("");
        answerText.setText("NOT DOG !");
        answerText.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
    }

    private void doShare() {
        Logger.d("");
        Uri uri = Uri.parse(AppConst.URL_OF_SHARE);
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);

    }

    private void updateBackground(Bitmap bitmap) {
        if (contentsImage != null) {
            contentsImage.setImageBitmap(bitmap);
        }
    }

    private void updateLabels(List<Label> labels) {
        if (labels == null || listsText == null) {
            return;
        }

        if (labels.size() == 0) {
            Toast.makeText(getApplicationContext(), "could not detect objects", Toast.LENGTH_SHORT).show();
            this.labels = null;
            return;
        }

        this.labels = labels;

        listsText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent2));

        boolean isFound = false;
        for (Label label : labels) {
            Logger.d(label.getName() + ": " + label.getConfidence().toString());
            listsText.setText(listsText.getText() + label.getName() + "......" + label.getConfidence().toString() + "%" + "\n");

            if (label.getName().equals(getString(R.string.string_dog))) {
                isFound = true;
                doFoundDog(label.getConfidence().toString());
            } else if (label.getName().equals(getString(R.string.string_hotdog))) {
                isFound = true;
                doFoundHotdog(label.getConfidence().toString());
            }
        }
        if (!isFound) {
            doNotDog();
        }
    }
}


