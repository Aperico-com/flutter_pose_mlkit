part of 'plugin_pose.dart';

// To indicate the format of image while creating input image from bytes
enum InputImageFormat { NV21, YV12, YUV_420_888, BGRA_8888 }

//To specify whether tflite models are stored in asset directory or file stored in device
enum CustomTrainedModel { asset, file }

//The camera rotation angle to be specified
enum InputImageRotation {
  Rotation_0deg,
  Rotation_90deg,
  Rotation_180deg,
  Rotation_270deg
}

///[InputImage] is the format Google' Ml kit takes to process the image
class InputImage {
  InputImage._(
      {String filePath,
        Uint8List bytes,
        @required String imageType,
        InputImageData inputImageData})
      : filePath = filePath,
        bytes = bytes,
        imageType = imageType,
        inputImageData = inputImageData;

  //Create InputImage from path of image stored in device
  factory InputImage.fromFilePath(String path) {
    assert(path != null);
    print("creating InputImage");
    return InputImage._(filePath: path, imageType: 'file');
  }

  //Create InputImage by passing a file
  factory InputImage.fromFile(File file) {
    assert(file != null);
    return InputImage._(filePath: file.path, imageType: 'file');
  }

  //Create InputImage using bytes
  factory InputImage.fromBytes(
      {@required Uint8List bytes,
        @required InputImageData inputImageData,
        String path}) {
    assert(bytes != null);
    assert(inputImageData != null);
    return InputImage._(
        bytes: bytes,
        imageType: 'bytes',
        inputImageData: inputImageData,
        filePath: path);
  }

  final String filePath;
  final Uint8List bytes;
  final String imageType;
  final InputImageData inputImageData;

  Map<String, dynamic> _getImageData() {
    var map = <String, dynamic>{
      'bytes': bytes,
      'type': imageType,
      'path': filePath,
      'metadata': inputImageData == null ? 'none' : inputImageData.getMetaData()
    };
    return map;
  }
}

//Data of image required when creating image from bytes
class InputImageData {
  //Size of image
  final Size size;

  //Image rotation degree
  final InputImageRotation imageRotation;

  final InputImageFormat inputImageFormat;

  InputImageData(
      {this.size,
        this.imageRotation,
        this.inputImageFormat = InputImageFormat.NV21});

  //Function to get the metadata of image processing purposes
  Map<String, dynamic> getMetaData() {
    var map = <String, dynamic>{
      'width': size.width,
      'height': size.height,
      'rotation': _imageRotationToInt(imageRotation),
      'imageFormat': _imageFormatToInt(inputImageFormat)
    };
    return map;
  }
}



int _imageFormatToInt(InputImageFormat inputImageFormat) {
  switch (inputImageFormat) {
    case InputImageFormat.NV21:
      return 17;
    case InputImageFormat.YV12:
      return 842094169;
    case InputImageFormat.YUV_420_888:
      return 35;
    case InputImageFormat.BGRA_8888:
      return -1;
    default:
      return 17;
  }
}

//Function to convert enum [InputImageRotation] to integer value

int _imageRotationToInt(InputImageRotation inputImageRotation) {
  switch (inputImageRotation) {
    case InputImageRotation.Rotation_0deg:
      return 0;
      break;
    case InputImageRotation.Rotation_90deg:
      return 90;
      break;
    case InputImageRotation.Rotation_180deg:
      return 180;
      break;
    case InputImageRotation.Rotation_270deg:
      return 270;
      break;
    default:
      return 0;
  }
}
