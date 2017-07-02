package com.unoemon.notdog.util;


import android.graphics.Bitmap;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.unoemon.notdog.MainApplication;
import com.unoemon.notdog.log.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import io.reactivex.Observable;

import static com.unoemon.notdog.util.AwsConst.IDENTITY_POOL_ID;

/**
 * Api util
 *
 */

public class ApiUtil {

    private static AmazonRekognitionClient amazonRekognitionClient;

    public static Observable<List<Label>> getDetectLabels(Bitmap bitmap) {
        return Observable.create(subscriber -> {

            // Create Client
            if (amazonRekognitionClient == null) {
                makeAmazonRekognitionClient();
            }

            // Create Image
            ByteBuffer imageBytes = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = ByteBuffer.wrap(baos.toByteArray());

            // Request List
            DetectLabelsRequest request = new DetectLabelsRequest().withImage(new Image().withBytes(imageBytes)).withMaxLabels(10).withMinConfidence(77F);
            DetectLabelsResult result = amazonRekognitionClient.detectLabels(request);

            List<Label> labels = result.getLabels();
            Logger.d(labels.toString());

            subscriber.onNext(labels);
            subscriber.onComplete();
        });
    }

    private static void makeAmazonRekognitionClient() {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                MainApplication.getInstance(),
                IDENTITY_POOL_ID, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        amazonRekognitionClient = new AmazonRekognitionClient(credentialsProvider);

    }


}
