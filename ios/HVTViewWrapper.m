#import "HVTViewWrapper.h"

@implementation HVTViewWrapper

- (void) willMoveToSuperview: (UIView *) newSuperview
{
  if(newSuperview == nil) {
    [self.viewManager viewWillBeRemoved:self];
  }
}

@end
