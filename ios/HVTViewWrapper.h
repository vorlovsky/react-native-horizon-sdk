#import <React/RCTComponent.h>
#import "HorizonSdkViewManager.h"

@import HorizonSDK;

@interface HVTViewWrapper: HVTView

@property (nonatomic, weak) HorizonSdkViewManager *viewManager;

@property (nonatomic, copy) RCTDirectEventBlock onStartedRunning;
@property (nonatomic, copy) RCTDirectEventBlock onStoppedRunning;
@property (nonatomic, copy) RCTDirectEventBlock onRecordingStarted;
@property (nonatomic, copy) RCTDirectEventBlock onRecordingFinished;

@end
