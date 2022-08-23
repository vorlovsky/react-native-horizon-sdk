//
//  CameraHelper.h
//  TestModule
//
//  Created by Admin on 20.08.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>

@import HorizonSDK;

@interface CameraHelper: NSObject {
}
+ (AVCaptureDevicePosition) getCameraPositionFacing:(nonnull NSNumber*)cameraFacing;
+ (HVTCaptureMode) getCameraCaptureMode:(nonnull NSString*)cameraMode;
+ (UIDeviceOrientation) getOrientation:(nonnull NSNumber*)screenRotation;
+ (BOOL) hasCamera:(nonnull NSNumber*)cameraFacing;
+ (nullable AVCaptureDevice*) getCamera:(nonnull NSNumber*)cameraFacing;
+ (nonnull NSMutableArray*) getSupportedVideoSize:(nonnull NSNumber*)cameraFacing;
@end
