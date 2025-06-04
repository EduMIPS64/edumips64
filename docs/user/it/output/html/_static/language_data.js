/*
 * This script contains the language-specific data used by searchtools.js,
 * namely the list of stopwords, stemmer, scorer and splitter.
 */

var stopwords = ["a", "abbia", "abbiamo", "abbiano", "abbiate", "ad", "agl", "agli", "ai", "al", "all", "alla", "alle", "allo", "anche", "avemmo", "avendo", "avesse", "avessero", "avessi", "avessimo", "aveste", "avesti", "avete", "aveva", "avevamo", "avevano", "avevate", "avevi", "avevo", "avrai", "avranno", "avrebbe", "avrebbero", "avrei", "avremmo", "avremo", "avreste", "avresti", "avrete", "avr\u00e0", "avr\u00f2", "avuta", "avute", "avuti", "avuto", "c", "che", "chi", "ci", "coi", "col", "come", "con", "contro", "cui", "da", "dagl", "dagli", "dai", "dal", "dall", "dalla", "dalle", "dallo", "degl", "degli", "dei", "del", "dell", "della", "delle", "dello", "di", "dov", "dove", "e", "ebbe", "ebbero", "ebbi", "ed", "era", "erano", "eravamo", "eravate", "eri", "ero", "essendo", "faccia", "facciamo", "facciano", "facciate", "faccio", "facemmo", "facendo", "facesse", "facessero", "facessi", "facessimo", "faceste", "facesti", "faceva", "facevamo", "facevano", "facevate", "facevi", "facevo", "fai", "fanno", "farai", "faranno", "farebbe", "farebbero", "farei", "faremmo", "faremo", "fareste", "faresti", "farete", "far\u00e0", "far\u00f2", "fece", "fecero", "feci", "fosse", "fossero", "fossi", "fossimo", "foste", "fosti", "fu", "fui", "fummo", "furono", "gli", "ha", "hai", "hanno", "ho", "i", "il", "in", "io", "l", "la", "le", "lei", "li", "lo", "loro", "lui", "ma", "mi", "mia", "mie", "miei", "mio", "ne", "negl", "negli", "nei", "nel", "nell", "nella", "nelle", "nello", "noi", "non", "nostra", "nostre", "nostri", "nostro", "o", "per", "perch\u00e9", "pi\u00f9", "quale", "quanta", "quante", "quanti", "quanto", "quella", "quelle", "quelli", "quello", "questa", "queste", "questi", "questo", "sarai", "saranno", "sarebbe", "sarebbero", "sarei", "saremmo", "saremo", "sareste", "saresti", "sarete", "sar\u00e0", "sar\u00f2", "se", "sei", "si", "sia", "siamo", "siano", "siate", "siete", "sono", "sta", "stai", "stanno", "starai", "staranno", "starebbe", "starebbero", "starei", "staremmo", "staremo", "stareste", "staresti", "starete", "star\u00e0", "star\u00f2", "stava", "stavamo", "stavano", "stavate", "stavi", "stavo", "stemmo", "stesse", "stessero", "stessi", "stessimo", "steste", "stesti", "stette", "stettero", "stetti", "stia", "stiamo", "stiano", "stiate", "sto", "su", "sua", "sue", "sugl", "sugli", "sui", "sul", "sull", "sulla", "sulle", "sullo", "suo", "suoi", "ti", "tra", "tu", "tua", "tue", "tuo", "tuoi", "tutti", "tutto", "un", "una", "uno", "vi", "voi", "vostra", "vostre", "vostri", "vostro", "\u00e8"];


/* Non-minified version is copied as a separate JS file, if available */
/**@constructor*/
BaseStemmer = function() {
    this.setCurrent = function(value) {
        this.current = value;
        this.cursor = 0;
        this.limit = this.current.length;
        this.limit_backward = 0;
        this.bra = this.cursor;
        this.ket = this.limit;
    };

    this.getCurrent = function() {
        return this.current;
    };

    this.copy_from = function(other) {
        this.current          = other.current;
        this.cursor           = other.cursor;
        this.limit            = other.limit;
        this.limit_backward   = other.limit_backward;
        this.bra              = other.bra;
        this.ket              = other.ket;
    };

    this.in_grouping = function(s, min, max) {
        if (this.cursor >= this.limit) return false;
        var ch = this.current.charCodeAt(this.cursor);
        if (ch > max || ch < min) return false;
        ch -= min;
        if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) return false;
        this.cursor++;
        return true;
    };

    this.in_grouping_b = function(s, min, max) {
        if (this.cursor <= this.limit_backward) return false;
        var ch = this.current.charCodeAt(this.cursor - 1);
        if (ch > max || ch < min) return false;
        ch -= min;
        if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) return false;
        this.cursor--;
        return true;
    };

    this.out_grouping = function(s, min, max) {
        if (this.cursor >= this.limit) return false;
        var ch = this.current.charCodeAt(this.cursor);
        if (ch > max || ch < min) {
            this.cursor++;
            return true;
        }
        ch -= min;
        if ((s[ch >>> 3] & (0X1 << (ch & 0x7))) == 0) {
            this.cursor++;
            return true;
        }
        return false;
    };

    this.out_grouping_b = function(s, min, max) {
        if (this.cursor <= this.limit_backward) return false;
        var ch = this.current.charCodeAt(this.cursor - 1);
        if (ch > max || ch < min) {
            this.cursor--;
            return true;
        }
        ch -= min;
        if ((s[ch >>> 3] & (0x1 << (ch & 0x7))) == 0) {
            this.cursor--;
            return true;
        }
        return false;
    };

    this.eq_s = function(s)
    {
        if (this.limit - this.cursor < s.length) return false;
        if (this.current.slice(this.cursor, this.cursor + s.length) != s)
        {
            return false;
        }
        this.cursor += s.length;
        return true;
    };

    this.eq_s_b = function(s)
    {
        if (this.cursor - this.limit_backward < s.length) return false;
        if (this.current.slice(this.cursor - s.length, this.cursor) != s)
        {
            return false;
        }
        this.cursor -= s.length;
        return true;
    };

    /** @return {number} */ this.find_among = function(v)
    {
        var i = 0;
        var j = v.length;

        var c = this.cursor;
        var l = this.limit;

        var common_i = 0;
        var common_j = 0;

        var first_key_inspected = false;

        while (true)
        {
            var k = i + ((j - i) >>> 1);
            var diff = 0;
            var common = common_i < common_j ? common_i : common_j; // smaller
            // w[0]: string, w[1]: substring_i, w[2]: result, w[3]: function (optional)
            var w = v[k];
            var i2;
            for (i2 = common; i2 < w[0].length; i2++)
            {
                if (c + common == l)
                {
                    diff = -1;
                    break;
                }
                diff = this.current.charCodeAt(c + common) - w[0].charCodeAt(i2);
                if (diff != 0) break;
                common++;
            }
            if (diff < 0)
            {
                j = k;
                common_j = common;
            }
            else
            {
                i = k;
                common_i = common;
            }
            if (j - i <= 1)
            {
                if (i > 0) break; // v->s has been inspected
                if (j == i) break; // only one item in v

                // - but now we need to go round once more to get
                // v->s inspected. This looks messy, but is actually
                // the optimal approach.

                if (first_key_inspected) break;
                first_key_inspected = true;
            }
        }
        do {
            var w = v[i];
            if (common_i >= w[0].length)
            {
                this.cursor = c + w[0].length;
                if (w.length < 4) return w[2];
                var res = w[3](this);
                this.cursor = c + w[0].length;
                if (res) return w[2];
            }
            i = w[1];
        } while (i >= 0);
        return 0;
    };

    // find_among_b is for backwards processing. Same comments apply
    this.find_among_b = function(v)
    {
        var i = 0;
        var j = v.length

        var c = this.cursor;
        var lb = this.limit_backward;

        var common_i = 0;
        var common_j = 0;

        var first_key_inspected = false;

        while (true)
        {
            var k = i + ((j - i) >> 1);
            var diff = 0;
            var common = common_i < common_j ? common_i : common_j;
            var w = v[k];
            var i2;
            for (i2 = w[0].length - 1 - common; i2 >= 0; i2--)
            {
                if (c - common == lb)
                {
                    diff = -1;
                    break;
                }
                diff = this.current.charCodeAt(c - 1 - common) - w[0].charCodeAt(i2);
                if (diff != 0) break;
                common++;
            }
            if (diff < 0)
            {
                j = k;
                common_j = common;
            }
            else
            {
                i = k;
                common_i = common;
            }
            if (j - i <= 1)
            {
                if (i > 0) break;
                if (j == i) break;
                if (first_key_inspected) break;
                first_key_inspected = true;
            }
        }
        do {
            var w = v[i];
            if (common_i >= w[0].length)
            {
                this.cursor = c - w[0].length;
                if (w.length < 4) return w[2];
                var res = w[3](this);
                this.cursor = c - w[0].length;
                if (res) return w[2];
            }
            i = w[1];
        } while (i >= 0);
        return 0;
    };

    /* to replace chars between c_bra and c_ket in this.current by the
     * chars in s.
     */
    this.replace_s = function(c_bra, c_ket, s)
    {
        var adjustment = s.length - (c_ket - c_bra);
        this.current = this.current.slice(0, c_bra) + s + this.current.slice(c_ket);
        this.limit += adjustment;
        if (this.cursor >= c_ket) this.cursor += adjustment;
        else if (this.cursor > c_bra) this.cursor = c_bra;
        return adjustment;
    };

    this.slice_check = function()
    {
        if (this.bra < 0 ||
            this.bra > this.ket ||
            this.ket > this.limit ||
            this.limit > this.current.length)
        {
            return false;
        }
        return true;
    };

    this.slice_from = function(s)
    {
        var result = false;
        if (this.slice_check())
        {
            this.replace_s(this.bra, this.ket, s);
            result = true;
        }
        return result;
    };

    this.slice_del = function()
    {
        return this.slice_from("");
    };

    this.insert = function(c_bra, c_ket, s)
    {
        var adjustment = this.replace_s(c_bra, c_ket, s);
        if (c_bra <= this.bra) this.bra += adjustment;
        if (c_bra <= this.ket) this.ket += adjustment;
    };

    this.slice_to = function()
    {
        var result = '';
        if (this.slice_check())
        {
            result = this.current.slice(this.bra, this.ket);
        }
        return result;
    };

    this.assign_to = function()
    {
        return this.current.slice(0, this.limit);
    };
};

// Generated by Snowball 2.1.0 - https://snowballstem.org/

/**@constructor*/
ItalianStemmer = function() {
    var base = new BaseStemmer();
    /** @const */ var a_0 = [
        ["", -1, 7],
        ["qu", 0, 6],
        ["\u00E1", 0, 1],
        ["\u00E9", 0, 2],
        ["\u00ED", 0, 3],
        ["\u00F3", 0, 4],
        ["\u00FA", 0, 5]
    ];

    /** @const */ var a_1 = [
        ["", -1, 3],
        ["I", 0, 1],
        ["U", 0, 2]
    ];

    /** @const */ var a_2 = [
        ["la", -1, -1],
        ["cela", 0, -1],
        ["gliela", 0, -1],
        ["mela", 0, -1],
        ["tela", 0, -1],
        ["vela", 0, -1],
        ["le", -1, -1],
        ["cele", 6, -1],
        ["gliele", 6, -1],
        ["mele", 6, -1],
        ["tele", 6, -1],
        ["vele", 6, -1],
        ["ne", -1, -1],
        ["cene", 12, -1],
        ["gliene", 12, -1],
        ["mene", 12, -1],
        ["sene", 12, -1],
        ["tene", 12, -1],
        ["vene", 12, -1],
        ["ci", -1, -1],
        ["li", -1, -1],
        ["celi", 20, -1],
        ["glieli", 20, -1],
        ["meli", 20, -1],
        ["teli", 20, -1],
        ["veli", 20, -1],
        ["gli", 20, -1],
        ["mi", -1, -1],
        ["si", -1, -1],
        ["ti", -1, -1],
        ["vi", -1, -1],
        ["lo", -1, -1],
        ["celo", 31, -1],
        ["glielo", 31, -1],
        ["melo", 31, -1],
        ["telo", 31, -1],
        ["velo", 31, -1]
    ];

    /** @const */ var a_3 = [
        ["ando", -1, 1],
        ["endo", -1, 1],
        ["ar", -1, 2],
        ["er", -1, 2],
        ["ir", -1, 2]
    ];

    /** @const */ var a_4 = [
        ["ic", -1, -1],
        ["abil", -1, -1],
        ["os", -1, -1],
        ["iv", -1, 1]
    ];

    /** @const */ var a_5 = [
        ["ic", -1, 1],
        ["abil", -1, 1],
        ["iv", -1, 1]
    ];

    /** @const */ var a_6 = [
        ["ica", -1, 1],
        ["logia", -1, 3],
        ["osa", -1, 1],
        ["ista", -1, 1],
        ["iva", -1, 9],
        ["anza", -1, 1],
        ["enza", -1, 5],
        ["ice", -1, 1],
        ["atrice", 7, 1],
        ["iche", -1, 1],
        ["logie", -1, 3],
        ["abile", -1, 1],
        ["ibile", -1, 1],
        ["usione", -1, 4],
        ["azione", -1, 2],
        ["uzione", -1, 4],
        ["atore", -1, 2],
        ["ose", -1, 1],
        ["ante", -1, 1],
        ["mente", -1, 1],
        ["amente", 19, 7],
        ["iste", -1, 1],
        ["ive", -1, 9],
        ["anze", -1, 1],
        ["enze", -1, 5],
        ["ici", -1, 1],
        ["atrici", 25, 1],
        ["ichi", -1, 1],
        ["abili", -1, 1],
        ["ibili", -1, 1],
        ["ismi", -1, 1],
        ["usioni", -1, 4],
        ["azioni", -1, 2],
        ["uzioni", -1, 4],
        ["atori", -1, 2],
        ["osi", -1, 1],
        ["anti", -1, 1],
        ["amenti", -1, 6],
        ["imenti", -1, 6],
        ["isti", -1, 1],
        ["ivi", -1, 9],
        ["ico", -1, 1],
        ["ismo", -1, 1],
        ["oso", -1, 1],
        ["amento", -1, 6],
        ["imento", -1, 6],
        ["ivo", -1, 9],
        ["it\u00E0", -1, 8],
        ["ist\u00E0", -1, 1],
        ["ist\u00E8", -1, 1],
        ["ist\u00EC", -1, 1]
    ];

    /** @const */ var a_7 = [
        ["isca", -1, 1],
        ["enda", -1, 1],
        ["ata", -1, 1],
        ["ita", -1, 1],
        ["uta", -1, 1],
        ["ava", -1, 1],
        ["eva", -1, 1],
        ["iva", -1, 1],
        ["erebbe", -1, 1],
        ["irebbe", -1, 1],
        ["isce", -1, 1],
        ["ende", -1, 1],
        ["are", -1, 1],
        ["ere", -1, 1],
        ["ire", -1, 1],
        ["asse", -1, 1],
        ["ate", -1, 1],
        ["avate", 16, 1],
        ["evate", 16, 1],
        ["ivate", 16, 1],
        ["ete", -1, 1],
        ["erete", 20, 1],
        ["irete", 20, 1],
        ["ite", -1, 1],
        ["ereste", -1, 1],
        ["ireste", -1, 1],
        ["ute", -1, 1],
        ["erai", -1, 1],
        ["irai", -1, 1],
        ["isci", -1, 1],
        ["endi", -1, 1],
        ["erei", -1, 1],
        ["irei", -1, 1],
        ["assi", -1, 1],
        ["ati", -1, 1],
        ["iti", -1, 1],
        ["eresti", -1, 1],
        ["iresti", -1, 1],
        ["uti", -1, 1],
        ["avi", -1, 1],
        ["evi", -1, 1],
        ["ivi", -1, 1],
        ["isco", -1, 1],
        ["ando", -1, 1],
        ["endo", -1, 1],
        ["Yamo", -1, 1],
        ["iamo", -1, 1],
        ["avamo", -1, 1],
        ["evamo", -1, 1],
        ["ivamo", -1, 1],
        ["eremo", -1, 1],
        ["iremo", -1, 1],
        ["assimo", -1, 1],
        ["ammo", -1, 1],
        ["emmo", -1, 1],
        ["eremmo", 54, 1],
        ["iremmo", 54, 1],
        ["immo", -1, 1],
        ["ano", -1, 1],
        ["iscano", 58, 1],
        ["avano", 58, 1],
        ["evano", 58, 1],
        ["ivano", 58, 1],
        ["eranno", -1, 1],
        ["iranno", -1, 1],
        ["ono", -1, 1],
        ["iscono", 65, 1],
        ["arono", 65, 1],
        ["erono", 65, 1],
        ["irono", 65, 1],
        ["erebbero", -1, 1],
        ["irebbero", -1, 1],
        ["assero", -1, 1],
        ["essero", -1, 1],
        ["issero", -1, 1],
        ["ato", -1, 1],
        ["ito", -1, 1],
        ["uto", -1, 1],
        ["avo", -1, 1],
        ["evo", -1, 1],
        ["ivo", -1, 1],
        ["ar", -1, 1],
        ["ir", -1, 1],
        ["er\u00E0", -1, 1],
        ["ir\u00E0", -1, 1],
        ["er\u00F2", -1, 1],
        ["ir\u00F2", -1, 1]
    ];

    /** @const */ var /** Array<int> */ g_v = [17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 128, 8, 2, 1];

    /** @const */ var /** Array<int> */ g_AEIO = [17, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 128, 8, 2];

    /** @const */ var /** Array<int> */ g_CG = [17];

    var /** number */ I_p2 = 0;
    var /** number */ I_p1 = 0;
    var /** number */ I_pV = 0;


    /** @return {boolean} */
    function r_prelude() {
        var /** number */ among_var;
        var /** number */ v_1 = base.cursor;
        while(true)
        {
            var /** number */ v_2 = base.cursor;
            lab0: {
                base.bra = base.cursor;
                among_var = base.find_among(a_0);
                if (among_var == 0)
                {
                    break lab0;
                }
                base.ket = base.cursor;
                switch (among_var) {
                    case 1:
                        if (!base.slice_from("\u00E0"))
                        {
                            return false;
                        }
                        break;
                    case 2:
                        if (!base.slice_from("\u00E8"))
                        {
                            return false;
                        }
                        break;
                    case 3:
                        if (!base.slice_from("\u00EC"))
                        {
                            return false;
                        }
                        break;
                    case 4:
                        if (!base.slice_from("\u00F2"))
                        {
                            return false;
                        }
                        break;
                    case 5:
                        if (!base.slice_from("\u00F9"))
                        {
                            return false;
                        }
                        break;
                    case 6:
                        if (!base.slice_from("qU"))
                        {
                            return false;
                        }
                        break;
                    case 7:
                        if (base.cursor >= base.limit)
                        {
                            break lab0;
                        }
                        base.cursor++;
                        break;
                }
                continue;
            }
            base.cursor = v_2;
            break;
        }
        base.cursor = v_1;
        while(true)
        {
            var /** number */ v_3 = base.cursor;
            lab1: {
                golab2: while(true)
                {
                    var /** number */ v_4 = base.cursor;
                    lab3: {
                        if (!(base.in_grouping(g_v, 97, 249)))
                        {
                            break lab3;
                        }
                        base.bra = base.cursor;
                        lab4: {
                            var /** number */ v_5 = base.cursor;
                            lab5: {
                                if (!(base.eq_s("u")))
                                {
                                    break lab5;
                                }
                                base.ket = base.cursor;
                                if (!(base.in_grouping(g_v, 97, 249)))
                                {
                                    break lab5;
                                }
                                if (!base.slice_from("U"))
                                {
                                    return false;
                                }
                                break lab4;
                            }
                            base.cursor = v_5;
                            if (!(base.eq_s("i")))
                            {
                                break lab3;
                            }
                            base.ket = base.cursor;
                            if (!(base.in_grouping(g_v, 97, 249)))
                            {
                                break lab3;
                            }
                            if (!base.slice_from("I"))
                            {
                                return false;
                            }
                        }
                        base.cursor = v_4;
                        break golab2;
                    }
                    base.cursor = v_4;
                    if (base.cursor >= base.limit)
                    {
                        break lab1;
                    }
                    base.cursor++;
                }
                continue;
            }
            base.cursor = v_3;
            break;
        }
        return true;
    };

    /** @return {boolean} */
    function r_mark_regions() {
        I_pV = base.limit;
        I_p1 = base.limit;
        I_p2 = base.limit;
        var /** number */ v_1 = base.cursor;
        lab0: {
            lab1: {
                var /** number */ v_2 = base.cursor;
                lab2: {
                    if (!(base.in_grouping(g_v, 97, 249)))
                    {
                        break lab2;
                    }
                    lab3: {
                        var /** number */ v_3 = base.cursor;
                        lab4: {
                            if (!(base.out_grouping(g_v, 97, 249)))
                            {
                                break lab4;
                            }
                            golab5: while(true)
                            {
                                lab6: {
                                    if (!(base.in_grouping(g_v, 97, 249)))
                                    {
                                        break lab6;
                                    }
                                    break golab5;
                                }
                                if (base.cursor >= base.limit)
                                {
                                    break lab4;
                                }
                                base.cursor++;
                            }
                            break lab3;
                        }
                        base.cursor = v_3;
                        if (!(base.in_grouping(g_v, 97, 249)))
                        {
                            break lab2;
                        }
                        golab7: while(true)
                        {
                            lab8: {
                                if (!(base.out_grouping(g_v, 97, 249)))
                                {
                                    break lab8;
                                }
                                break golab7;
                            }
                            if (base.cursor >= base.limit)
                            {
                                break lab2;
                            }
                            base.cursor++;
                        }
                    }
                    break lab1;
                }
                base.cursor = v_2;
                if (!(base.out_grouping(g_v, 97, 249)))
                {
                    break lab0;
                }
                lab9: {
                    var /** number */ v_6 = base.cursor;
                    lab10: {
                        if (!(base.out_grouping(g_v, 97, 249)))
                        {
                            break lab10;
                        }
                        golab11: while(true)
                        {
                            lab12: {
                                if (!(base.in_grouping(g_v, 97, 249)))
                                {
                                    break lab12;
                                }
                                break golab11;
                            }
                            if (base.cursor >= base.limit)
                            {
                                break lab10;
                            }
                            base.cursor++;
                        }
                        break lab9;
                    }
                    base.cursor = v_6;
                    if (!(base.in_grouping(g_v, 97, 249)))
                    {
                        break lab0;
                    }
                    if (base.cursor >= base.limit)
                    {
                        break lab0;
                    }
                    base.cursor++;
                }
            }
            I_pV = base.cursor;
        }
        base.cursor = v_1;
        var /** number */ v_8 = base.cursor;
        lab13: {
            golab14: while(true)
            {
                lab15: {
                    if (!(base.in_grouping(g_v, 97, 249)))
                    {
                        break lab15;
                    }
                    break golab14;
                }
                if (base.cursor >= base.limit)
                {
                    break lab13;
                }
                base.cursor++;
            }
            golab16: while(true)
            {
                lab17: {
                    if (!(base.out_grouping(g_v, 97, 249)))
                    {
                        break lab17;
                    }
                    break golab16;
                }
                if (base.cursor >= base.limit)
                {
                    break lab13;
                }
                base.cursor++;
            }
            I_p1 = base.cursor;
            golab18: while(true)
            {
                lab19: {
                    if (!(base.in_grouping(g_v, 97, 249)))
                    {
                        break lab19;
                    }
                    break golab18;
                }
                if (base.cursor >= base.limit)
                {
                    break lab13;
                }
                base.cursor++;
            }
            golab20: while(true)
            {
                lab21: {
                    if (!(base.out_grouping(g_v, 97, 249)))
                    {
                        break lab21;
                    }
                    break golab20;
                }
                if (base.cursor >= base.limit)
                {
                    break lab13;
                }
                base.cursor++;
            }
            I_p2 = base.cursor;
        }
        base.cursor = v_8;
        return true;
    };

    /** @return {boolean} */
    function r_postlude() {
        var /** number */ among_var;
        while(true)
        {
            var /** number */ v_1 = base.cursor;
            lab0: {
                base.bra = base.cursor;
                among_var = base.find_among(a_1);
                if (among_var == 0)
                {
                    break lab0;
                }
                base.ket = base.cursor;
                switch (among_var) {
                    case 1:
                        if (!base.slice_from("i"))
                        {
                            return false;
                        }
                        break;
                    case 2:
                        if (!base.slice_from("u"))
                        {
                            return false;
                        }
                        break;
                    case 3:
                        if (base.cursor >= base.limit)
                        {
                            break lab0;
                        }
                        base.cursor++;
                        break;
                }
                continue;
            }
            base.cursor = v_1;
            break;
        }
        return true;
    };

    /** @return {boolean} */
    function r_RV() {
        if (!(I_pV <= base.cursor))
        {
            return false;
        }
        return true;
    };

    /** @return {boolean} */
    function r_R1() {
        if (!(I_p1 <= base.cursor))
        {
            return false;
        }
        return true;
    };

    /** @return {boolean} */
    function r_R2() {
        if (!(I_p2 <= base.cursor))
        {
            return false;
        }
        return true;
    };

    /** @return {boolean} */
    function r_attached_pronoun() {
        var /** number */ among_var;
        base.ket = base.cursor;
        if (base.find_among_b(a_2) == 0)
        {
            return false;
        }
        base.bra = base.cursor;
        among_var = base.find_among_b(a_3);
        if (among_var == 0)
        {
            return false;
        }
        if (!r_RV())
        {
            return false;
        }
        switch (among_var) {
            case 1:
                if (!base.slice_del())
                {
                    return false;
                }
                break;
            case 2:
                if (!base.slice_from("e"))
                {
                    return false;
                }
                break;
        }
        return true;
    };

    /** @return {boolean} */
    function r_standard_suffix() {
        var /** number */ among_var;
        base.ket = base.cursor;
        among_var = base.find_among_b(a_6);
        if (among_var == 0)
        {
            return false;
        }
        base.bra = base.cursor;
        switch (among_var) {
            case 1:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                break;
            case 2:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                var /** number */ v_1 = base.limit - base.cursor;
                lab0: {
                    base.ket = base.cursor;
                    if (!(base.eq_s_b("ic")))
                    {
                        base.cursor = base.limit - v_1;
                        break lab0;
                    }
                    base.bra = base.cursor;
                    if (!r_R2())
                    {
                        base.cursor = base.limit - v_1;
                        break lab0;
                    }
                    if (!base.slice_del())
                    {
                        return false;
                    }
                }
                break;
            case 3:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_from("log"))
                {
                    return false;
                }
                break;
            case 4:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_from("u"))
                {
                    return false;
                }
                break;
            case 5:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_from("ente"))
                {
                    return false;
                }
                break;
            case 6:
                if (!r_RV())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                break;
            case 7:
                if (!r_R1())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                var /** number */ v_2 = base.limit - base.cursor;
                lab1: {
                    base.ket = base.cursor;
                    among_var = base.find_among_b(a_4);
                    if (among_var == 0)
                    {
                        base.cursor = base.limit - v_2;
                        break lab1;
                    }
                    base.bra = base.cursor;
                    if (!r_R2())
                    {
                        base.cursor = base.limit - v_2;
                        break lab1;
                    }
                    if (!base.slice_del())
                    {
                        return false;
                    }
                    switch (among_var) {
                        case 1:
                            base.ket = base.cursor;
                            if (!(base.eq_s_b("at")))
                            {
                                base.cursor = base.limit - v_2;
                                break lab1;
                            }
                            base.bra = base.cursor;
                            if (!r_R2())
                            {
                                base.cursor = base.limit - v_2;
                                break lab1;
                            }
                            if (!base.slice_del())
                            {
                                return false;
                            }
                            break;
                    }
                }
                break;
            case 8:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                var /** number */ v_3 = base.limit - base.cursor;
                lab2: {
                    base.ket = base.cursor;
                    if (base.find_among_b(a_5) == 0)
                    {
                        base.cursor = base.limit - v_3;
                        break lab2;
                    }
                    base.bra = base.cursor;
                    if (!r_R2())
                    {
                        base.cursor = base.limit - v_3;
                        break lab2;
                    }
                    if (!base.slice_del())
                    {
                        return false;
                    }
                }
                break;
            case 9:
                if (!r_R2())
                {
                    return false;
                }
                if (!base.slice_del())
                {
                    return false;
                }
                var /** number */ v_4 = base.limit - base.cursor;
                lab3: {
                    base.ket = base.cursor;
                    if (!(base.eq_s_b("at")))
                    {
                        base.cursor = base.limit - v_4;
                        break lab3;
                    }
                    base.bra = base.cursor;
                    if (!r_R2())
                    {
                        base.cursor = base.limit - v_4;
                        break lab3;
                    }
                    if (!base.slice_del())
                    {
                        return false;
                    }
                    base.ket = base.cursor;
                    if (!(base.eq_s_b("ic")))
                    {
                        base.cursor = base.limit - v_4;
                        break lab3;
                    }
                    base.bra = base.cursor;
                    if (!r_R2())
                    {
                        base.cursor = base.limit - v_4;
                        break lab3;
                    }
                    if (!base.slice_del())
                    {
                        return false;
                    }
                }
                break;
        }
        return true;
    };

    /** @return {boolean} */
    function r_verb_suffix() {
        if (base.cursor < I_pV)
        {
            return false;
        }
        var /** number */ v_2 = base.limit_backward;
        base.limit_backward = I_pV;
        base.ket = base.cursor;
        if (base.find_among_b(a_7) == 0)
        {
            base.limit_backward = v_2;
            return false;
        }
        base.bra = base.cursor;
        if (!base.slice_del())
        {
            return false;
        }
        base.limit_backward = v_2;
        return true;
    };

    /** @return {boolean} */
    function r_vowel_suffix() {
        var /** number */ v_1 = base.limit - base.cursor;
        lab0: {
            base.ket = base.cursor;
            if (!(base.in_grouping_b(g_AEIO, 97, 242)))
            {
                base.cursor = base.limit - v_1;
                break lab0;
            }
            base.bra = base.cursor;
            if (!r_RV())
            {
                base.cursor = base.limit - v_1;
                break lab0;
            }
            if (!base.slice_del())
            {
                return false;
            }
            base.ket = base.cursor;
            if (!(base.eq_s_b("i")))
            {
                base.cursor = base.limit - v_1;
                break lab0;
            }
            base.bra = base.cursor;
            if (!r_RV())
            {
                base.cursor = base.limit - v_1;
                break lab0;
            }
            if (!base.slice_del())
            {
                return false;
            }
        }
        var /** number */ v_2 = base.limit - base.cursor;
        lab1: {
            base.ket = base.cursor;
            if (!(base.eq_s_b("h")))
            {
                base.cursor = base.limit - v_2;
                break lab1;
            }
            base.bra = base.cursor;
            if (!(base.in_grouping_b(g_CG, 99, 103)))
            {
                base.cursor = base.limit - v_2;
                break lab1;
            }
            if (!r_RV())
            {
                base.cursor = base.limit - v_2;
                break lab1;
            }
            if (!base.slice_del())
            {
                return false;
            }
        }
        return true;
    };

    this.stem = /** @return {boolean} */ function() {
        var /** number */ v_1 = base.cursor;
        r_prelude();
        base.cursor = v_1;
        r_mark_regions();
        base.limit_backward = base.cursor; base.cursor = base.limit;
        var /** number */ v_3 = base.limit - base.cursor;
        r_attached_pronoun();
        base.cursor = base.limit - v_3;
        var /** number */ v_4 = base.limit - base.cursor;
        lab0: {
            lab1: {
                var /** number */ v_5 = base.limit - base.cursor;
                lab2: {
                    if (!r_standard_suffix())
                    {
                        break lab2;
                    }
                    break lab1;
                }
                base.cursor = base.limit - v_5;
                if (!r_verb_suffix())
                {
                    break lab0;
                }
            }
        }
        base.cursor = base.limit - v_4;
        var /** number */ v_6 = base.limit - base.cursor;
        r_vowel_suffix();
        base.cursor = base.limit - v_6;
        base.cursor = base.limit_backward;
        var /** number */ v_7 = base.cursor;
        r_postlude();
        base.cursor = v_7;
        return true;
    };

    /**@return{string}*/
    this['stemWord'] = function(/**string*/word) {
        base.setCurrent(word);
        this.stem();
        return base.getCurrent();
    };
};

Stemmer = ItalianStemmer;
