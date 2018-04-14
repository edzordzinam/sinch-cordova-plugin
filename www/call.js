/*global cordova, module*/
var emptyFnc = function () {
};

module.exports = {
    makeVOIPCall: function (user_id, number, customer_name, booking_id, customer_address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Call", "makeVOIPCall", [user_id, number, customer_name, booking_id, customer_address]);
    }
};
