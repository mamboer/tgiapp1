define({
    codeName:'G3',
    //https://api.github.com/repos/mamboer/3gz/contents/
    //http://levinhuang-pc0.tencent.com:8081/github/mamboer/3gz/
    remoteUrl:function(){

        var url = location.href;
        url = url.substr(0,url.lastIndexOf('3gz/')+4);
        return url;
    },
    defaultController:'home',
    defaultAction:'index'
});