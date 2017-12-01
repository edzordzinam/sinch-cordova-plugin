/*global cordova, module*/
var emptyFnc = function(){};

module.exports = {
    makePhoneCall: function (number, customer_name, booking_id, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Call", "makePhoneCall", [number, customer_name, booking_id]);
    },
    initSinchClient: function (userId, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Call", "initSinchClient", [userId]);
    },
    hangUp: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Call", "hangUp", []);
    },


    onCallStarted: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw 'call#onCallStarted requires a success callback';
        }
        cordova.exec(success,
            failure || emptyFnc,
            'Call',
            'onCallStarted', []);
    },

    onCallEnded: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw 'call#onCallEnded requires a success callback';
        }
        cordova.exec(success,
            failure || emptyFnc,
            'Call',
            'onCallEnded', []);
    },

    onCallEstablished: function(success, failure) {
        cordova.exec(success || emptyFnc,
            failure || emptyFnc,
            'Call',
            'onCallEstablished', []);
    },

    onCallInProgress: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw 'call#onCallInProgress requires a success callback';
        }
        cordova.exec(success,
            failure || emptyFnc,
            'Call',
            'onCallInProgress', []);
    },

    onCallClientStarted: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw 'call#onCallClientStarted requires a success callback';
        }
        cordova.exec(success,
            failure || emptyFnc,
            'Call',
            'onCallClientStarted', []);
    },

    isClientStarted: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw 'call#isClientStarted requires a success callback';
        }
        cordova.exec(success,
            failure || emptyFnc,
            'Call',
            'isClientStarted', []);
    }

};
