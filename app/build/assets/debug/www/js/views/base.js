/**
 * views/base
 * @requires Hogan,PUSH
 * @return {Object} view base module
 */
define(function(){

    var toHtml =  function(tpl,obj,ext){tpl = Hogan.compile(tpl);return (tpl.render(obj,ext));},
        evtName = 'viewRendered';

    window.addEventListener(evtName, function(e){
        PUSH.updateDomCache(e.detail.state.id);
    });

    var mod = {
        render:function(tpl,obj,ext){
            return toHtml(tpl,obj,ext);
        },
        /** 
         * render template to specified target
         * @param {Object} target dom target
         * @param {Object} opts options, {tpl:'xxx',tplData:{x:1,y;2},ext:{},triggerEvent:false,eventData:{}}
         */
        renderTo:function(target,opts){
            var html = this.render(opts.tpl,opts.tplData,opts.ext),
                bars = PUSH.bars,
                dom = target,
                con = null,
                result = {};

            if(dom){
                dom.innerHTML = html;
            }else{
                dom = document.body;
                con = dom.querySelector('.content');
                if( !con ){
                    dom.innerHTML = html;
                }else{
                    opts.eventData.transition = opts.eventData.transition||true;
                    result = PUSH.parseXHR({responseText:html},opts.eventData);

                    //update bars
                    Object.keys(bars).forEach(function (key) {
                      var el = dom.querySelector(bars[key]);
                      if ( el && result[key] ) {
                          el.innerHTML = result[key].innerHTML;
                          html = html.replace(el.innerHTML,'');
                      }
                    });

                    //update contents
                    result.contents && ( con.innerHTML = result.contents.innerHTML );

                    //modals
                    $.each(result.modals||[],function(i,o){
                        dom.appendChild(o);
                    });

                }
            };

            if(opts.triggerEvent){
                this.dispatchRenderedEvent(opts.eventData);
            }
        },
        dispatchRenderedEvent:function(data){
            var e = new CustomEvent(evtName, {
                detail: { state: data },
                bubbles: true,
                cancelable: true
            });

            window.dispatchEvent(e);
        }
    };

    return mod;

});