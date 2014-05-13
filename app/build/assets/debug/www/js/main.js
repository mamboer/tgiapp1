require.config({
    baseUrl:'../../js',
    paths: {

        text: 'libs/require/plugins/text',
        domReady: 'libs/require/plugins/domReady'
    },

    waitSeconds: 10

});

require([
    // Load our app module and pass it to our definition function
    'index'
], function(app){

    var appData = {
        
        developer:      'Levin Wong',
        developerSite:  'FASO.ME',

        splashDelay:1000

    };

    // The "app" dependency is passed in as "App"
    app.init(appData);

});