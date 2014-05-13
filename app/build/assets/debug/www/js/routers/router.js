define([
    'controllers/base'
],function(CBase){
    
    var onPushed = function(e){
        CBase.applyControllerByUrl(e.detail.state.url,e.detail.state);
    };

    window.addEventListener('push', onPushed);

    var mod = {
        //manually navigating to a specified url
        navigate:function(url){
            //创建一个状态信息数据
            //参考Push.js
            var state = {
                url:url,
                id:new Date().getTime()
            };
            onPushed({
                detail:{
                    state:state
                }
            });
        }
    };

    return mod;

});