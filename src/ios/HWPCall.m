#import "HWPCall.h"

#import <Sinch/Sinch.h>

@implementation HWPCall

NSString* callInProgressCallbackId;
NSString* callEstablishedCallbackId;
NSString* callEndedCallbackId;
NSString* callStartedCallbackId;
NSString* callClientStartedCallbackId;
NSString* phoneNumber;
NSString* customerName;
NSNumber* bookingId;

- (void)initSinchClient:(CDVInvokedUrlCommand*)command {
     NSString* msg = @"success";
     NSString* userId = [[command arguments] objectAtIndex:0];

    if (!_client) {
        _client = [Sinch clientWithApplicationKey:@"1dd3b012-c381-4baa-b835-729895c2e976"
                                applicationSecret:@"I7zmATPS/0aXQG87XrmG0g=="
                                  environmentHost:@"clientapi.sinch.com"
                                           userId:userId];

        _client.delegate = self;
        //_client.callClient.delegate = self;
        [_client setSupportCalling:YES];
        [_client start];

    }

    CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)makePhoneCall:(CDVInvokedUrlCommand*)command
{

    NSString* number = [[command arguments] objectAtIndex:0];
    NSString* msg = @"success";


    phoneNumber = number;
    customerName = [[command arguments] objectAtIndex:1];
    bookingId = [NSNumber numberWithInt:[[command arguments] objectAtIndex:2]];

     _call = [_client.callClient callPhoneNumber:number];
     _call.delegate = self;

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)hangUp:(CDVInvokedUrlCommand*)command
{

    NSString* msg = @"success";

    [_call hangup];
    _call = nil;

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)callReceivedOnRemoteEnd:(id<SINCall>)call {
    NSString* soundFilePath = [[NSBundle mainBundle] pathForResource:@"progresstone" ofType:@"wav"];
    // get audio controller from SINClient
    _audioController = [_client audioController];
    [_audioController startPlayingSoundFile:soundFilePath loop:NO];
}


- (void)onDurationTimer:(NSTimer *)unused {
    NSInteger duration = [[NSDate date] timeIntervalSinceDate:[[_call details] establishedTime]];
    [self setDuration:duration];
}


- (void)setCallStatusText:(NSString *)text {
    NSLog(@"%@", text);
}


#pragma mark - Event callbacks

- (void) onCallStarted:(CDVInvokedUrlCommand*)command
{
    callStartedCallbackId = command.callbackId;
}

- (void) onCallInProgress:(CDVInvokedUrlCommand*)command
{
    callInProgressCallbackId = command.callbackId;
}

- (void) onCallEstablished:(CDVInvokedUrlCommand *)command
{
    callEstablishedCallbackId = command.callbackId;
}

- (void) onCallEnded:(CDVInvokedUrlCommand *)command
{
    callEndedCallbackId = command.callbackId;
}

- (void) onCallClientStarted:(CDVInvokedUrlCommand*)command
{
    callClientStartedCallbackId = command.callbackId;
}

#pragma mark - Duration

- (void)setDuration:(NSInteger)seconds {
    //sending seconds to Javascript Side for updates.

    if (callInProgressCallbackId != nil) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsNSInteger:seconds];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callInProgressCallbackId];
    }
}

- (void)internal_updateDuration:(NSTimer *)timer {
    SEL selector = NSSelectorFromString([timer userInfo]);
    if ([self respondsToSelector:selector]) {
    #pragma clang diagnostic push
    #pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [self performSelector:selector withObject:timer];
    #pragma clang diagnostic pop
    }
}

- (void)startCallDurationTimerWithSelector:(SEL)sel {
    NSString *selectorAsString = NSStringFromSelector(sel);
    self.durationTimer = [NSTimer scheduledTimerWithTimeInterval:0.5
                                                          target:self
                                                        selector:@selector(internal_updateDuration:)
                                                        userInfo:selectorAsString
                                                         repeats:YES];
}

- (void)stopCallDurationTimer {
    [self.durationTimer invalidate];
    self.durationTimer = nil;
}


#pragma mark - SINClientDelegate

- (void)clientDidStart:(id<SINClient>)client {
    NSLog(@"Sinch client started successfully (version: %@)", [Sinch version]);
    if (callClientStartedCallbackId != nil) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callClientStartedCallbackId];
    }
}

- (void)clientDidFail:(id<SINClient>)client error:(NSError *)error {
    NSLog(@"Sinch client error: %@", [error localizedDescription]);
}

- (void)client:(id<SINClient>)client
    logMessage:(NSString *)message
          area:(NSString *)area
      severity:(SINLogSeverity)severity
     timestamp:(NSDate *)timestamp {
    //NSLog(@"%@", message);
}


#pragma mark - SINCallDelegate

- (void)callDidProgress:(id<SINCall>)call {
    //in this method you can play ringing tone adn update ui to display progress of call.
    NSLog(@"CAlling in progress");
    [self callReceivedOnRemoteEnd:call];

    //send call established event
    if (callStartedCallbackId != nil) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callStartedCallbackId];
    }

}

- (void)callDidEstablish:(id<SINCall>)call {
    //Called when a call connects.
    NSLog(@"CAll established");
    [self startCallDurationTimerWithSelector:@selector(onDurationTimer:)];
    [_audioController stopPlayingSoundFile];

    //send call established event
    if (callEstablishedCallbackId != nil) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callEstablishedCallbackId];
    }

}

- (void)callDidEnd:(id<SINCall>)call {
    //called when call finnished. or no response was received
    _call = nil;
    NSLog(@"Hanged Up Call");
    [_audioController stopPlayingSoundFile];
    [self stopCallDurationTimer];

    NSDictionary *payload;

    if ([[call details] startedTime] != nil){
        double startedTime = [[[call details] startedTime] timeIntervalSince1970];
        double establishedTime = [[[call details] establishedTime] timeIntervalSince1970];
        double endedTime = [[[call details] endedTime] timeIntervalSince1970];

         payload = @{@"startedTime": [NSNumber numberWithDouble:startedTime],
                                  @"endedTime": [NSNumber numberWithDouble:endedTime],
                                  @"establishedTime":[NSNumber numberWithDouble:establishedTime],
                                  @"phonenumber": phoneNumber,
                                  @"customer_name" : customerName,
                                  @"booking_id" : bookingId};
    }else {
         payload = @{@"startedTime": @0,
                                  @"endedTime": @0,
                                  @"establishedTime": @0,
                                  @"phonenumber": phoneNumber,
                                  @"customer_name" : customerName,
                                  @"booking_id" : bookingId};
    }



    //send call established event
    if (callEndedCallbackId != nil) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:payload];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:callEndedCallbackId];
    }

}



@end
