define([
    'utils/consts',
    'utils/base64'
],(function(C,B64){

   var mod = {
        url:C.remoteUrl()+'src/tpl/shared/splash.html',
        cacheKey:C.codeName+'.tpl-shared-splash',
        init:function(cbk){
            var localObj = localStorage[mod.cacheKey],
                remoteObj;

            if(localObj){
                localObj = B64.decode(localObj);
            };

            $.ajax({
                url:mod.url,
                error:function(xhr,errType,err){
                    cbk && cbk(err,localObj);
                },
                success:function(data,status,xhr){
                    if(data.content){
                        //for github api testing:https://developer.github.com/v3/repos/contents/
                        remoteObj = B64.decode(data.content);
                        //update localStorage
                        localStorage[mod.cacheKey] = data.content;
                    }else{
                        remoteObj = data;
                        localStorage[mod.cacheKey] = B64.encode(data);
                    }

                    localObj = localObj || remoteObj;

                    cbk && cbk(null,remoteObj,localObj);

                }
            });
        },
        hide:function(){
            $('#g3splash').hide();
        }
   };

   return mod;


}));