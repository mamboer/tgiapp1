define([
    'utils/consts'
],function(C){

    var mod = {
        cache:{},
        add:function(key,data){
            this.cache[key] = data;
            localStorage[key] = data;
        },
        get:function(key){
            return (this.cache[key]||localStorage[key]);
        },
        getByUrl:function(tplUrl){
            return this.get(this.getCacheKey(tplUrl));
        },
        getCacheKey:function(tplUrl){
            var flag = 'tpl/';
            tplUrl = tplUrl.indexOf(flag)===-1?(flag+tplUrl):tplUrl;
            flag = C.codeName+'.'+tplUrl.replace('.html','').substr(tplUrl.indexOf(flag)).replace(/\//gi,'-');
            return flag;
        }
    };

    return mod;

});