define([
    'utils/consts',
    'utils/version',
    'utils/tplProvider'
],function(C,V,tplProvider){


    var mod = {
        queue : new createjs.LoadQueue(true),
        onComplete:function(){
            //TODO:
            this.cbk&&this.cbk();
        },
        onProgress:function(e){
            var t = Math.round(e.progress * 100 * .999);
            if (t == 0) t = 1;
            this.$progress.css({
                width: t + "%"
            });
        },
        onFileLoad:function(e){
            var item = e.item,
                type = item.type;

            switch(type){
                case createjs.LoadQueue.TEXT:
                    tplProvider.add(item.cacheKey,e.result);
                break;
            };
        },
        init:function(cbk){
            this.cbk = cbk;
            this.queue.on("complete", this.onComplete, this);
            this.queue.on("progress", this.onProgress, this);
            this.queue.on("fileload", this.onFileLoad, this);
            this.$progress = $('#g3progress');
            this.run();
        },
        run:function(){
            V.check(function(err,newVersion,oldVersion){
                
                //this.$progress = $("#rlb-progress");
                

                //获取远程版本文件出错
                if(err) {
                    oldVersion = newVersion;
                }
                //无版本更新
                if( oldVersion.version === newVersion.version ){
                    console.log('preload','无版本更新');
                    mod.onProgress({
                        progress:1
                    });
                    mod.onComplete();
                    return;
                }

                //获取需要预加载的文件
                mod.loadTpl(newVersion.preload.tpl,oldVersion.preload.tpl);

            });
        },
        getTplCacheKey:function(tplUrl){
            return tplProvider.getCacheKey(tplUrl);
        },
        loadTpl:function(newTpls,oldTpls){
            var len1 = newTpls.length,
                len0 = oldTpls.length,
                tpls = [],
                tpl1,tpl0,tpl2;

            //获取待更新的模板
            for(var i=0;i<len1;i++){
                tpl1 = newTpls[i];
                tpl1.cacheKey = mod.getTplCacheKey(tpl1.src);
                tpl2 = null;
                for(var j=0;j<len0;j++){
                    tpl0 = oldTpls[j];
                    if(!tpl0.cacheKey){
                        tpl0.cacheKey = mod.getTplCacheKey( tpl0.src );
                    };
                    if( tpl0.cacheKey !== tpl1.cacheKey ){
                        continue;
                    };
                    if( tpl0.version === tpl1.version ){
                        tpl2 = tpl1;
                        break;
                    };
                };

                if(!tpl2){
                    tpl1.src = C.remoteUrl()+tpl1.src;
                    tpls.push(tpl1);
                }
            };

            console.log('preload - 下载模板...',tpls);

            mod.queue.loadManifest(tpls);

        }

    };

    return ({
        init:function(cbk){
            mod.init(cbk);
        }
    });

});