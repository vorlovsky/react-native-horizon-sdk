#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import <React/RCTLog.h>
#import "HorizonSdkViewManager.h"
#import "HVTViewWrapper.h"
#import "CameraHelper.h"

@import HorizonSDK;

@implementation HorizonSdkViewManager

- (void)setCameraFacingTowards:(NSNumber *)cameraFacing
{
  AVCaptureDevicePosition position = cameraFacing == 0 ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront;
  [self.camera setCameraPosition:position error:NULL];
}

RCT_EXPORT_MODULE(HorizonSdkView)

RCT_EXPORT_VIEW_PROPERTY(onStartedRunning, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onStoppedRunning, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onRecordingStarted, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onRecordingFinished, RCTDirectEventBlock)

- (UIView *)view
{
  if(!self.camera) {
    self.camera = [HVTCamera new];
    [self.camera setDelegate:self];
    
    [self setCameraFacingTowards:0];

    RCTLogInfo(@"camera created");
  }

  HVTViewWrapper *preview = [[HVTViewWrapper alloc] initWithFrame:CGRectZero];
  preview.viewManager = self;
  
  [self.camera addView: preview];

  if (!self.previews) {
      self.previews = [[NSMutableArray alloc] init];
  }
  [self.previews addObject: preview];

  return preview;
}

RCT_EXPORT_METHOD(startRunning:(nonnull NSNumber*)reactTag) {
  [self.camera startRunning];
}

RCT_EXPORT_METHOD(stopRunning:(nonnull NSNumber*)reactTag) {
  [self.camera stopRunning];

  for (HVTViewWrapper *preview in self.previews) {
    if (!preview.onStoppedRunning) {
      return;
    }

    preview.onStoppedRunning(@{});
  }
}

RCT_EXPORT_METHOD(setCamera:(nonnull NSNumber*)reactTag 
              facingTowards:(nonnull NSNumber*)cameraFacing 
              withVideoSize:(nullable NSArray*)videoSize 
              withPhotoSize:(nullable NSArray*)photoSize) {
    // [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        // HVTViewWrapper *view = (HVTViewWrapper *)viewRegistry[reactTag];
        // if (!view) {
        //     RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
        //     return;
        // }

        //[self setCameraImpl:view facingTowards:cameraFacing withVideoSize:videoSize withPhotoSize:photoSize];
        AVCaptureDevicePosition position = [CameraHelper getCameraPositionFacing:cameraFacing];

        if(videoSize) {
          CGSize resolution = CGSizeMake([videoSize[0] doubleValue], [videoSize[1] doubleValue]);

          NSError *error = nil;
          [self.camera setCameraPosition:position withResolution:resolution error:&error];

          if(error) {
            NSLog(@"%@",[error localizedDescription]);
          }
        } else {
          NSError *error = nil;
          [self.camera setCameraPosition:position error:&error];

          if(error) {
            NSLog(@"%@",[error localizedDescription]);
          }
        }
    // }];
}

RCT_EXPORT_METHOD(startRecording:(nonnull NSNumber*)reactTag 
                      targetPath:(NSString*)path) {
  if(self.camera.isRecording) {
    return;
  }
  
  self.recordingPath = path;

  NSURL *url = [NSURL fileURLWithPath:path];

  [self.camera startRecordingWithMovieURL:url];
}

RCT_EXPORT_METHOD(stopRecording:(nonnull NSNumber*)reactTag) {
  [self.camera stopRecording];
}

RCT_CUSTOM_VIEW_PROPERTY(cameraMode, NSString, HVTViewWrapper)
{  
  self.camera.captureMode = [CameraHelper getCameraCaptureMode:json];
}

RCT_CUSTOM_VIEW_PROPERTY(screenRotation, NSNumber, HVTViewWrapper)
{
  self.camera.interfaceOrientation = [CameraHelper getOrientation:json];
}

RCT_CUSTOM_VIEW_PROPERTY(previewDisabled, NSNumber, HVTViewWrapper)
{
  view.enabled = ![json boolValue];
}

RCT_CUSTOM_VIEW_PROPERTY(tapToFocus, NSNumber, HVTViewWrapper)
{
  view.enablesTapToFocus = [json boolValue];
}

// RCT_CUSTOM_VIEW_PROPERTY(color, NSString, UIView)
// {
//   [view setBackgroundColor:[self hexStringToColor:json]];
// }

// - hexStringToColor:(NSString *)stringToConvert
// {
//   NSString *noHashString = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@""];
//   NSScanner *stringScanner = [NSScanner scannerWithString:noHashString];

//   unsigned hex;
//   if (![stringScanner scanHexInt:&hex]) return nil;
//   int r = (hex >> 16) & 0xFF;
//   int g = (hex >> 8) & 0xFF;
//   int b = (hex) & 0xFF;

//   return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:1.0f];
// }

#pragma mark - HVTViewWrapper callback

- (void)viewWillBeRemoved:(HVTView *)view {
  ((HVTViewWrapper *)view).viewManager = nil;
  [self.previews removeObject:view];
  [self.camera removeView:view];
}

#pragma mark - HVTCameraDelegate

- (void)hvtCameraDidStartRunning:(HVTCamera *)hvtCamera { 
  for (HVTViewWrapper * preview in self.previews) {
    if (!preview.onStartedRunning) {
      return;
    }

    preview.onStartedRunning(@{});
  }  
}

- (void)hvtCamera:(HVTCamera *)hvtCamera didStopRunningWithError:(NSError *)error {
  for (HVTViewWrapper * preview in self.previews) {
    if (!preview.onStoppedRunning) {
      return;
    }

    preview.onStoppedRunning(@{@"error":error});
  }
}

- (void)hvtCameraRecordingDidStart:(HVTCamera *)hvtCamera {
  for (HVTViewWrapper * preview in self.previews) {
    if (!preview.onRecordingStarted) {
      return;
    }

    preview.onRecordingStarted(@{});
  }
}

- (void)hvtCamera:(HVTCamera *)hvtCamera recordingDidFailWithError:(NSError *)error {
  for (HVTViewWrapper * preview in self.previews) {
    if (!preview.onRecordingFinished) {
      return;
    }

    preview.onRecordingFinished(@{@"path": self.recordingPath, @"success": @NO});
  }
}

- (void)hvtCamera:(HVTCamera *)hvtCamera didStopRecordingWithMetadata:(NSDictionary*)metadata {
  for (HVTViewWrapper * preview in self.previews) {
    if (!preview.onRecordingFinished) {
      return;
    }

    preview.onRecordingFinished(@{@"path": self.recordingPath, @"success": @YES});
  }
}

- (void)hvtCamera:(HVTCamera *)hvtCamera didUpdateParams:(HVTParams)params { } // mandatory delegate method

@end
