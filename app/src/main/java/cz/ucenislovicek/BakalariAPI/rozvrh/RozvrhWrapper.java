// taken from https://github.com/vitSkalicky/lepsi-rozvrh/
package cz.ucenislovicek.BakalariAPI.rozvrh;




import cz.ucenislovicek.BakalariAPI.rozvrh.items.Rozvrh;

public class RozvrhWrapper {
    public static final int SOURCE_MEMORY = 1;
    public static final int SOURCE_CACHE = 2;
    public static final int SOURCE_NET = 3;

    private final Rozvrh rozvrh;

    private final int code;

    private final int source;

    public RozvrhWrapper(Rozvrh rozvrh, int code, int source) {
        this.rozvrh = rozvrh;
        this.code = code;
        this.source = source;
    }

    public Rozvrh getRozvrh() {
        return rozvrh;
    }

    public int getCode() {
        return code;
    }

    public int getSource() {
        return source;
    }
}
