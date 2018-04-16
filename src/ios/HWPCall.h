#import <Cordova/CDV.h>

@interface HWPCall : CDVPlugin

- (void) makeVOIPCall:(CDVInvokedUrlCommand*)command;

@property (nonatomic, readwrite, strong) NSTimer *durationTimer;

@end
