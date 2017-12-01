#import <Cordova/CDV.h>
#import <Sinch/Sinch.h>

id<SINClient> _client;
id<SINCall> _call;
id<SINAudioController> _audioController;

@interface HWPCall : CDVPlugin <SINClientDelegate, SINCallDelegate>

- (void)setCallStatusText:(NSString *)text;

- (void)setDuration:(NSInteger)seconds;
- (void)startCallDurationTimerWithSelector:(SEL)sel;
- (void)stopCallDurationTimer;

- (void) makePhoneCall:(CDVInvokedUrlCommand*)command;
- (void) initSinchClient:(CDVInvokedUrlCommand*)command;
- (void) hangUp:(CDVInvokedUrlCommand*)command;
- (void) onCallStarted:(CDVInvokedUrlCommand*)command;
- (void) onCallInProgress:(CDVInvokedUrlCommand*)command;
- (void) onCallEstablished:(CDVInvokedUrlCommand*)command;
- (void) onCallEnded:(CDVInvokedUrlCommand*)command;
- (void) onCallClientStarted:(CDVInvokedUrlCommand*)command;

@property (nonatomic, readwrite, strong) NSTimer *durationTimer;

@end