/**
 * UA detect module
 * @requires zepto.detect
 */
(function() {
    function formatV(v, split) {
        if (v == null) {
            return;
        }
        split = split || '.';
        var f = v.split(split);
        f = f.shift() + '.' + f.join('');
        return parseFloat(f);
    }
    $.os.fVersion = formatV($.os.version);

    var tag = '';
    if ($.os.ios) {
        tag = 'ios ios' + $.os.version.replace(/\./gi, '_');
        if ($.os.fVersion < 6) {
            tag += ' lowIPhone';
        } else {
            tag += ' highIPhone';
        }
    } else if ($.os.android) {
        tag = 'android android' + $.os.version.replace(/\./gi, '_');
        if ($.os.fVersion < 4) {
            tag += ' lowAndroid';
        } else {
            tag += ' highAndroid';
        }
    }
    if (tag) {
        $('html').addClass(tag);
    }
})();