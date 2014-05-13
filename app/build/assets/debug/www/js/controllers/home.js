define([
    'utils/tplProvider',
    'views/base',
    'controllers/base'
],function(tplProvider,V,C){

    var mod = {
        //action index
        index:function(opts){
            this.basic(opts);
        }
    };

    return C.merge(mod);

});