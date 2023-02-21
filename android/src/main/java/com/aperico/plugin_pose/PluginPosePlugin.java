package com.aperico.plugin_pose;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;



/** PluginPosePlugin */
public class PluginPosePlugin implements FlutterPlugin, MethodCallHandler {

  private static class MethodResultWrapper implements MethodChannel.Result {
    private MethodChannel.Result methodResult;
    private Handler handler;

    MethodResultWrapper(MethodChannel.Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.success(result);
                }
              });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.error(errorCode, errorMessage, errorDetails);
                }
              });
    }

    @Override
    public void notImplemented() {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.notImplemented();
                }
              });
    }
  }

  public Context applicationContext;
//
//  public PluginPosePlugin(Context applicationContext) {
//    this.applicationContext = applicationContext;
//  }

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private MyPoseDetector detector;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    applicationContext = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "plugin_pose");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result rawResult) {

    MethodChannel.Result result = new MethodResultWrapper(rawResult);

    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("startPoseDetector")) {
      detector = new MyPoseDetector( (Map<String, Object>)call.argument("options") );
      assert detector != null;
//      System.out.println("Detector started");
      result.success("Detector successfully started");
    } else if (call.method.equals("handleDetection")) {
      assert detector != null;
      InputImage inputImage;
      try {
        inputImage = getInputImage((Map<String, Object>) call.argument("imageData"), result);
      } catch (Exception e) {
        Log.e("ImageError", "Getting Image failed");
        e.printStackTrace();
        result.error("imageInputError", e.toString(), null);
        return;
      }
      detector.handleDetection( inputImage, result );
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private InputImage getInputImage(Map<String, Object> imageData, MethodChannel.Result result) {
    String model = (String) imageData.get("type");
    InputImage inputImage;
    if (model.equals("file")) {
      try {
        inputImage = InputImage.fromFilePath(applicationContext, Uri.fromFile(new File(((String) imageData.get("path")))));
        return inputImage;
      } catch (IOException e) {
        Log.e("ImageError", "Getting Image failed");
        e.printStackTrace();
        result.error("imageInputError", e.toString(), null);
        return null;
      }
    } else if (model.equals("bytes")) {
        Map<String, Object> metaData = (Map<String, Object>) imageData.get("metadata");
        inputImage = InputImage.fromByteArray((byte[]) imageData.get("bytes"),
                (int) (double) metaData.get("width"),
                (int) (double) metaData.get("height"),
                (int) metaData.get("rotation"),
                (int) metaData.get("imageFormat"));

        return inputImage;

    } else {
      new IOException("Error occurred");
      return null;
    }
  }
}
