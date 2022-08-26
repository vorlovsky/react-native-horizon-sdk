#import <React/RCTViewManager.h>

@import HorizonSDK;

@interface HorizonSdkViewManager : RCTViewManager <HVTCameraDelegate>

@property (nonatomic) HVTCamera *camera;
@property (nonatomic) NSMutableArray *previews;
@property (nonatomic) NSString *recordingPath;

- (void)viewWillBeRemoved:(HVTView *)view;

@end
