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

}

- (void)destroy {
     self.viewController = nil;
}



@end
