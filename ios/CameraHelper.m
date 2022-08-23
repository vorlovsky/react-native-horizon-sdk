//
//  CameraHelper.m
//  TestModule
//
//  Created by Admin on 20.08.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import "CameraHelper.h"

@import HorizonSDK;

@interface CameraHelper()
+ (BOOL) isCamera:(nonnull AVCaptureDevice*)device 
          facingTowards:(nonnull NSNumber*)cameraFacing; 
@end

@implementation CameraHelper
+ (AVCaptureDevicePosition) getCameraPositionFacing:(nonnull NSNumber*)cameraFacing 
{
    return ([cameraFacing intValue] == 0 ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront); 
}

+ (HVTCaptureMode) getCameraCaptureMode:(nonnull NSString*)cameraMode
{
    return [cameraMode isEqualToString:@"PICTURE"] ? HVTCaptureModePhoto : HVTCaptureModeVideo; 
}

+ (UIDeviceOrientation) getOrientation:(nonnull NSNumber*)screenRotation
{
    switch([screenRotation intValue]) {
        case 1:
            return UIDeviceOrientationLandscapeLeft;
        case 2:
            return UIDeviceOrientationPortraitUpsideDown;
        case 3:
            return UIDeviceOrientationLandscapeRight;
    }

    return UIDeviceOrientationPortrait;
}

+ (BOOL) isCamera:(nonnull AVCaptureDevice*)device
          facingTowards:(nonnull NSNumber*)cameraFacing 
{
    return ([device position] == [CameraHelper getCameraPositionFacing:cameraFacing]); 
}

+ (BOOL) hasCamera:(nonnull NSNumber*)cameraFacing
{
  AVCaptureDevicePosition position = [CameraHelper getCameraPositionFacing:cameraFacing];
  return [HVTCamera isCameraPositionSupported:position];
}

+ (nullable AVCaptureDevice*) getCamera:(nonnull NSNumber*)cameraFacing
{
    NSArray *devices = [AVCaptureDevice devices];

    for (AVCaptureDevice *device in devices) {
        if ([device hasMediaType:AVMediaTypeVideo]) {
            if ([CameraHelper isCamera:device facingTowards:cameraFacing]) {
                return device;
            }
        }
    }

    return nil;
}

+ (nonnull NSMutableArray*) getSupportedVideoSize:(nonnull NSNumber*)cameraFacing
{
  AVCaptureDevice *device = [CameraHelper getCamera:cameraFacing];
    
  NSMutableArray *result = [[NSMutableArray alloc] init];
  if (device) {
      NSLog(@"Device name: %@", [device localizedName]);
      for (AVCaptureDeviceFormat *format in device.formats) {
          CMVideoDimensions dimensions = CMVideoFormatDescriptionGetDimensions(format.formatDescription);
        //   NSLog(@"Dimentions: %ux%u", dimensions.width, dimensions.height);

          NSDictionary* dict = @{
            @"width": @(dimensions.width),
            @"height": @(dimensions.height),
            @"aspectRatio": @((float)dimensions.width/(float)dimensions.height),
          };

          [result addObject: dict];
      }
  }

  return result;
}

@end
