define([
    'utils/tplProvider',
    'views/base',
    'controllers/base'
],function(tplProvider,V,C){

    var mod = {
        //action index
        index:function(opts){
            this.basic(opts);
        },
        events:{
            'click':'#btnNoCache clearCache'
        },
        clearCache:function(){
            localStorage.clear();
            alert('Done clearing cache!');
            location.reload();
        }
    };

    return C.merge(mod);

});