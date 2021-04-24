#import <Cordova/CDV.h>
#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMessageComposeViewController.h>

@interface HWPCall : CDVPlugin <MFMessageComposeViewControllerDelegate>

@property(strong) NSString* callbackID;

- (void) makeVOIPCall:(CDVInvokedUrlCommand*)command;
- (void) sendSMS:(CDVInvokedUrlCommand*)command;

@property (nonatomic, readwrite, strong) NSTimer *durationTimer;

@end
