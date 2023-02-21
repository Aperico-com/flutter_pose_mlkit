
import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

part 'image_util.dart';
part 'pose_detector.dart';

class PluginPose {

  PluginPose._();

  static const MethodChannel _channel = const MethodChannel('plugin_pose');

  static final PluginPose instance = PluginPose._();

  PoseDetector poseDetector({PoseDetectorOptions poseDetectorOptions}) {
    return PoseDetector(poseDetectorOptions ?? PoseDetectorOptions());
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
