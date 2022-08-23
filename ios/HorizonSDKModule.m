#import "HorizonSdkModule.h"
#import <React/RCTLog.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHorizonSdkModuleSpec.h"
#endif

@import HorizonSDK;

@implementation HorizonSdkModule
RCT_EXPORT_MODULE()

BOOL initialized;

RCT_REMAP_METHOD(init,
                 initWithApiKey:(nonnull NSString*)apiKey
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    if(!initialized) {
        [[HVTSDK sharedInstance] activateWithAPIKey:apiKey];

        RCTLogInfo(@"HVTSDK initialized");

        initialized = YES;
    }
    
    NSNumber *result = [NSNumber numberWithBool: initialized];
        
    resolve(result);
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeHorizonSdkModuleSpecJSI>(params);
}
#endif

@end
