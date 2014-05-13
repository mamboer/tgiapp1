define([
        'routers/router',
        'utils/preload',
        'utils/juice',
        'controllers/splash'
], (function(router, preload,juice,splash){

    var mod = {
        bindEvents : function() {

            if ( juice.isPhoneGap() ) {

                document.addEventListener("deviceready", mod.onDeviceReady, false);

            } else {

                mod.onDeviceReady(); // Running is the browser

            }

            addEventListener('resetView', mod.onResetView);

        },
        onDeviceReady : function() {

            console.log('Received Event: onDeviceReady');

            // Build the main app view
            mod.initSplash();

        },
        onResetView : function() {

            

        },
        onAppReady : function(){

            try{

                navigator.splashscreen.hide();

            }catch (error){

                console.log('SplashScreen Hide Error', error.message);

            }

        },
        initSplash : function(){

            splash.init(function(err,newHtml,oldHtml){
                if(!err && newHtml){
                    document.body.innerHTML += newHtml;
                }
                //preload resources
                preload.init(function(err){

                    setTimeout(function(){
                        splash.hide();
                        router.navigate(location.href);
                    },mod.appData.splashDelay);

                });
            });

        },
        init:function(appData){
            this.appData = appData;
            this.bindEvents();
        }
    };

    return ({
        init:function(appData){
            mod.init(appData);
        }
    });
}));
