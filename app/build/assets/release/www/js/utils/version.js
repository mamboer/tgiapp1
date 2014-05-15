define(['utils/base64','utils/consts'],function(B64,C){

    var remoteUrl = C.remoteUrl()+'package.json',
        cacheKey = C.codeName+'.package';

    var request = function(cbk){
        var localObj = localStorage[cacheKey],
            remoteObj;

        if(localObj){
            localObj = JSON.parse(B64.decode(localObj));
        };

        $.ajax({
            url:remoteUrl,
            dataType:'json',
            error:function(xhr,errType,err){
                cbk && cbk(err,localObj);
            },
            success:function(data,status,xhr){
                if(data.content){
                    //for github api testing:https://developer.github.com/v3/repos/contents/
                    remoteObj = JSON.parse(B64.decode(data.content));
                    //update localStorage
                    localStorage[cacheKey] = data.content;
                }else{
                    remoteObj = data;
                    localStorage[cacheKey] = B64.encode(JSON.stringify(data));
                }

                //first cache copy
                if(!localObj){
                    localObj = {
                        "preload":{
                            "tpl":[]
                        }
                    };
                }

                cbk && cbk(null,remoteObj,localObj);

            }
        });
    };

    return ({
        check:request
    });

});