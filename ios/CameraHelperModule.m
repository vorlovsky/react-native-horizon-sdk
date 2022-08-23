#import "CameraHelperModule.h"
#import "CameraHelper.h"
#import <React/RCTLog.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNCameraHelperModuleSpec.h"
#endif

@import HorizonSDK;

@implementation CameraHelperModule

RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(hasCamera,
                 facingTowards:(nonnull NSNumber*)cameraFacing
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    AVCaptureDevice *device = [CameraHelper getCamera:cameraFacing];

    NSNumber *result = [NSNumber numberWithBool:device];

    resolve(result);
}

RCT_REMAP_METHOD(getSupportedVideoSize,
                 forCameraFacingTowards:(nonnull NSNumber*)cameraFacing
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    NSMutableArray *result = [CameraHelper getSupportedVideoSize:cameraFacing];

    resolve(result);
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeCameraHelperModuleSpecJSI>(params);
}
#endif

@end
