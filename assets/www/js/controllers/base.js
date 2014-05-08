define([
    'utils/consts',
    'views/base',
    'utils/tplProvider'
],function(C,V,tplProvider){
    var mod = {
        /**
         * 根据视图url获取对应的控制器路径。
         * 注意：视图url和控制器的路径是按照目录惯例进行组织的
         * @param  {string} url 视图URL
         * @return {string}     控制器路径
         */
        getControllerByUrl:function(url){

            //url generalization
            //特殊处理index.html
            url = url.lastIndexOf('.html')===-1? (url+C.defaultAction+'.html'):url;

            //views/home/manual.html
            var action = this.getActionByUrl(url),
                viewFlag = 'views/',
                ctrFlag = 'controllers/';

            url = url.substr(url.indexOf(viewFlag)+viewFlag.length,url.lastIndexOf('/'));
            url = url.substr(0,url.indexOf('/'));

            return ({
                controllerUrl:(ctrFlag+url),
                controller:url,
                action:action,
                actionUrl:(url+'/'+action)
            });

        },
        /**
         * 根据视图url获取对应控制器对应的action
         * @param  {string} url 视图url
         * @return {string}     action
         */
        getActionByUrl:function(url){
            url = url.substr(url.lastIndexOf('/')+1)
            url = url.substr(0,url.lastIndexOf('.html'));
            return url;
        },
        /**
         * 根据视图url调用对应的控制器
         * @param  {string} url 视图url
         * @param {string} opts 额外的配置信息
         */
        applyControllerByUrl:function(url,opts){
            var ctr = this.getControllerByUrl(url);
            opts = $.extend(opts||{},ctr);
            require([ctr.controllerUrl],function(CTR){
                CTR.init(opts);
            });
        },
        init:function(opts){
            var action = this[opts.action]||this.basic,
                $doc = $(document),
                evtPair = null,
                me = this;

            action.call(this,opts);

            if(this.events){
                for(var c in this.events){
                    evtPair = this.events[c].split(' ');
                    $doc.on(c,evtPair[0],function(e){
                        me[evtPair[1]].call(me,e);
                    });
                };
            };
        },
        //basic action
        basic:function(opts,data){
            var tpl = tplProvider.getByUrl(opts.actionUrl),
                target = null;//set target to null,let our view engine parse the html automatically

            V.renderTo(target,{
                tpl:tpl,
                tplData:data||{},
                triggerEvent:true,
                eventData:opts
            });
        },
        merge:function(controllerInstance){
            return $.extend(this,controllerInstance||{});
        }
    };

    return mod;

});