//
//  CallViewController.m
//  test
//
//  Created by Prexition on 14/04/2018.
//  Copyright © 2018 Prexition. All rights reserved.
//

#import <Sinch/Sinch.h>
#import "CallUIViewController.h"

@interface CallUIViewController ()

@end

@implementation CallUIViewController

NSString* callInProgressCallbackId;
NSString* callEstablishedCallbackId;
NSString* callEndedCallbackId;
NSString* callStartedCallbackId;
NSString* callClientStartedCallbackId;

- (void)close {
 
    [self stopCallDurationTimer];
    [_client stop];
    _client = nil;
    
    [self.lblStatus setText:@"Ending Call"];
    _call = nil;
    NSLog(@"Hanged Up Call");
    [_audioController stopPlayingSoundFile];
    [self stopCallDurationTimer];
    
    __weak UIViewController* weakSelf = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if([weakSelf respondsToSelector:@selector(presentingViewController)]) {
            [[weakSelf presentingViewController] dismissViewControllerAnimated:YES completion:nil];
        } else {
            [[weakSelf parentViewController] dismissViewControllerAnimated:YES completion:nil];
        }
        
        [weakSelf removeFromParentViewController];
        [weakSelf.navigationController removeFromParentViewController];
    });
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    [self.lblName setText:self.customerName];
    [self.lblAddress setText:self.customerAddress];
    
    //start the phone call
    if (!_client) {
         [self.lblStatus setText:@"Starting call service"];
        _client = [Sinch clientWithApplicationKey:@"XXXXXXXXXXXXX"
                                applicationSecret:@"XXXXXXXXXXXXX"
                                  environmentHost:@"clientapi.sinch.com"
                                           userId:self.userId];
        
        _client.delegate = self;
        [_client setSupportCalling:YES];
        [_client start];
    }
    
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)hangup:(id)sender{
    [_call hangup];
    _call = nil;
    [self close];
}

- (void)callReceivedOnRemoteEnd:(id<SINCall>)call {
    NSString* soundFilePath = [[NSBundle mainBundle] pathForResource:@"progresstone" ofType:@"wav"];
    // get audio controller from SINClient
    _audioController = [_client audioController];
    [_audioController startPlayingSoundFile:soundFilePath loop:YES];
}


- (void)onDurationTimer:(NSTimer *)unused {
    NSInteger duration = [[NSDate date] timeIntervalSinceDate:[[_call details] establishedTime]];
    [self setDuration:duration];
}


- (void)setCallStatusText:(NSString *)text {
    [self.lblStatus setText:text];
}

#pragma mark - Duration

- (void)setDuration:(NSInteger)seconds {
    //sending seconds to Javascript Side for updates.
    int sec = seconds % 60;
    int minutes = (seconds / 60) % 60;

    [self.lblDuration setText:[NSString stringWithFormat:@"%02d:%02d", minutes, sec]];
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
    
    //setting additional headers for the call
    NSDictionary *headers = @{
                              @"customer" : self.customerName,
                              @"address" : self.customerAddress,
                              @"number" : self.phoneNumber
                              };
    
    if (_client){
         [self.lblStatus setText:@"Initiating Call"];
        _call = [_client.callClient callPhoneNumber:self.phoneNumber
                                            headers:headers];
        _call.delegate = self;
    }
}

- (void)clientDidFail:(id<SINClient>)client error:(NSError *)error {
    [self.lblStatus setText:@"Error Connecting, hangup and retry"];
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
    [self.lblStatus setText:@"Initiating Call"];
}

- (void)callDidEstablish:(id<SINCall>)call {
    //Called when a call connects.
    NSLog(@"CAll established");
    [self startCallDurationTimerWithSelector:@selector(onDurationTimer:)];
    [_audioController stopPlayingSoundFile];
    [self.lblStatus setText:@"Call Connected"];
}

- (void)callDidEnd:(id<SINCall>)call {
    //called when call finnished. or no response was received
    [self close];
}

@end
