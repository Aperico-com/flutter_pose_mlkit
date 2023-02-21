package com.aperico.plugin_pose;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class MyPoseDetector {

    final private PoseDetector poseDetector;
    private String selectionType;
    private List<Integer> poseLandMarksList;

    MyPoseDetector(Map<String, Object> detectorOptions) {
        String detectorModel = (String) detectorOptions.get("detectorType");
        int detectorMode = (int) detectorOptions.get("detectorMode");
        poseLandMarksList = (List<Integer>) detectorOptions.get("landmarksList");
        selectionType = (String) detectorOptions.get("selections");
        if (detectorModel.equals("base")) {
            PoseDetectorOptions options = new PoseDetectorOptions.Builder().setDetectorMode(detectorMode).build();
            poseDetector = PoseDetection.getClient(options);
        } else {
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(detectorMode).build();
            poseDetector = PoseDetection.getClient(options);
        }
    }

    public void handleDetection(InputImage inputImage, final MethodChannel.Result result) {
        System.out.println("JNI handling detection");
        if (inputImage != null) {
            Task<Pose> poseTask =
                    poseDetector.process(inputImage)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Pose>() {
                                        @Override
                                        public void onSuccess(Pose pose) {
                                            List<Map<String,Object>> pointsList = new ArrayList<>();
                                            if (selectionType.equals("all")) {
                                                for(PoseLandmark poseLandmark : pose.getAllPoseLandmarks()){
                                                    Map<String,Object> poseLandmarkMap = new HashMap<>();
                                                    poseLandmarkMap.put("position",poseLandmark.getLandmarkType());
                                                    poseLandmarkMap.put("x",poseLandmark.getPosition().x);
                                                    poseLandmarkMap.put("y",poseLandmark.getPosition().y);
                                                    pointsList.add(poseLandmarkMap);
                                                }
                                            } else {
                                                for (int i : poseLandMarksList) {
                                                    Map<String,Object> poseLandmarkMap = new HashMap<>();
                                                    PoseLandmark poseLandmark = pose.getPoseLandmark(i);
                                                    poseLandmarkMap.put("position",poseLandmark.getLandmarkType());
                                                    poseLandmarkMap.put("x",poseLandmark.getPosition().x);
                                                    poseLandmarkMap.put("y",poseLandmark.getPosition().y);
                                                    pointsList.add(poseLandmarkMap);
                                                }
                                            }
                                            System.out.println("JNI pose detect SUCCESS");
                                            result.success(pointsList);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                            System.out.println("JNI pose detect FAILED");
                                            result.error("poseDetector error",e.toString(),null);
                                        }
                                    });
        }
        else{
            System.out.println("JNI pose detect NO IMAGE");
            result.error("poseDetector error", "No image to process!", null);
        }

    }

    public void closeDetector() throws IOException {
        poseDetector.close();
    }
}
