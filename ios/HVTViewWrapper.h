#import <React/RCTComponent.h>

@import HorizonSDK;

@class HVTViewWrapper; 
@protocol HVTViewWrapperDelegate <NSObject>
- (void)willBeRemoved:(HVTViewWrapper*)sender;
@end

@interface HVTViewWrapper: HVTView
@property (nonatomic, weak) id <HVTViewWrapperDelegate> delegate;
@property (nonatomic, copy) RCTDirectEventBlock onStartedRunning;
@property (nonatomic, copy) RCTDirectEventBlock onStoppedRunning;
@property (nonatomic, copy) RCTDirectEventBlock onRecordingStarted;
@property (nonatomic, copy) RCTDirectEventBlock onRecordingFinished;

@end