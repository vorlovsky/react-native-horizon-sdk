#import "HVTViewWrapper.h"

@implementation HVTViewWrapper
@synthesize delegate;

- (void) willMoveToSuperview: (UIView *) newSuperview
{
  if(newSuperview == nil) {
    [self.delegate willBeRemoved:self];
  }
}

@end
