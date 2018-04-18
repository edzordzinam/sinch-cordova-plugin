#import "HWPCall.h"

#import <Sinch/Sinch.h>
#import "CallUIViewController.h"

@implementation HWPCall

NSString* phoneNumber;
NSString* customerName;
NSNumber* bookingId;
NSString* userId;
NSString* customerAddress;


- (void)makeVOIPCall:(CDVInvokedUrlCommand*)command
{
    //get the user id;
    userId = [[command arguments] objectAtIndex:0];
    phoneNumber = [[command arguments] objectAtIndex:1];
    customerName = [[command arguments] objectAtIndex:2];
    bookingId = [NSNumber numberWithInt:[[command arguments] objectAtIndex:3]];
    customerAddress = [[command arguments] objectAtIndex:4];
    
    //loading the view from the storyboard
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"CallUI" bundle:nil];
    CallUIViewController *callUIViewController = [storyboard instantiateViewControllerWithIdentifier:@"CallUIViewController"];
    
    if(callUIViewController == nil) {
        callUIViewController = [[CallUIViewController alloc] init];
    }
    
    //passing data to the viewcontroller for UX display
    callUIViewController.userId = userId;
    callUIViewController.phoneNumber = phoneNumber;
    callUIViewController.customerName = customerName;
    callUIViewController.customerAddress = customerAddress;
    
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:callUIViewController animated:YES completion:nil];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:@"success"];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];


}


- (void)sendSMS:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* phoneNumber = [command.arguments objectAtIndex:0];
    NSString* textMessage = [command.arguments objectAtIndex:1];

        self.callbackID = command.callbackId;
        
        if(![MFMessageComposeViewController canSendText]) {
        NSMutableDictionary* returnInfo = [NSMutableDictionary dictionaryWithCapacity:2];
            
        [returnInfo setObject:@"SMS_FEATURE_NOT_SUPPORTED" forKey:@"code"];
        [returnInfo setObject:@"SMS feature is not supported on this device" forKey:@"message"];
                
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:returnInfo];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

            return;
        }
        
        MFMessageComposeViewController *composeViewController = [[MFMessageComposeViewController alloc] init];
        composeViewController.messageComposeDelegate = self;

        NSMutableArray *recipients = [[NSMutableArray alloc] init];
        
    [recipients addObject:phoneNumber];
            
    [composeViewController setBody:textMessage];
            [composeViewController setRecipients:recipients];

            [self.viewController presentViewController:composeViewController animated:YES completion:nil];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:@"success"];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

}

- (void)destroy {
     self.viewController = nil;
}


// Handle the different situations of MFMessageComposeViewControllerDelegate
- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    BOOL succeeded = NO;
    NSString* errorCode = @"";
    NSString* message = @"";
    
    switch(result) {
        case MessageComposeResultSent:
            succeeded = YES;
            message = @"Message sent";
            break;
        case MessageComposeResultCancelled:
            message = @"Message cancelled";
            errorCode = @"SMS_MESSAGE_CANCELLED";
            break;
        case MessageComposeResultFailed:
            message = @"Message Compose Result failed";
            errorCode = @"SMS_MESSAGE_COMPOSE_FAILED";
            break;
        default:
            message = @"Sms General error";
            errorCode = @"SMS_GENERAL_ERROR";
            break;
    }
    
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

@end
