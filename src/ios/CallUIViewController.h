//
//  CallUIViewController.h
//  test
//
//  Created by Prexition on 14/04/2018.
//  Copyright © 2018 Prexition. All rights reserved.
//

#import <Sinch/Sinch.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>


id<SINClient> _client;
id<SINCall> _call;
id<SINAudioController> _audioController;
AVAudioPlayer *avSound;

@interface CallUIViewController : UIViewController <SINClientDelegate, SINCallDelegate>

@property (nonatomic, strong) IBOutlet UILabel *nameLabel;
@property (nonatomic, assign) NSString *userId;
@property (nonatomic, assign) NSString *phoneNumber;
@property (nonatomic, assign) NSString *customerName;
@property (nonatomic, assign) NSString *customerAddress;

@property (weak, nonatomic) IBOutlet UILabel *lblName;

@property (weak, nonatomic) IBOutlet UILabel *lblAddress;
@property (weak, nonatomic) IBOutlet UILabel *lblDuration;
@property (weak, nonatomic) IBOutlet UILabel *lblStatus;

@property (nonatomic, readwrite, strong) NSTimer *durationTimer;

- (void)setCallStatusText:(NSString *)text;

- (void)setDuration:(NSInteger)seconds;
- (void)startCallDurationTimerWithSelector:(SEL)sel;
- (void)stopCallDurationTimer;


- (IBAction)hangup:(id)sender;

@end

