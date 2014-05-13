define(function(){

    return ({
        isMobile:function(){
            return navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/);
        },
        isPhoneGap:function(){
            return ( window["cordova"] || window["PhoneGap"] || window["phonegap"]) 
                && /^file:\/{3}[^\/]/i.test(window.location.href) 
                && this.isMobile();
        }
    });

});